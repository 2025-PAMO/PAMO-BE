package com.example.demo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.demo.apiPayload.code.GeneralErrorCode;
import com.example.demo.apiPayload.code.MusicErrorCode;
import com.example.demo.apiPayload.exception.CustomException;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.MusicSummary;
import com.example.demo.domain.User;
import com.example.demo.dto.music.MotionMusicRegenerateResponse;
import com.example.demo.dto.music.MusicDetailResponseDTO;
import com.example.demo.dto.music.MusicRegenerateResponse;
import com.example.demo.dto.user.UserProfileDTO;
import com.example.demo.repository.*;
import com.example.demo.repository.projection.RelatedItemView;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MusicService {

    private static final int RELATED_LIMIT = 6;
    private static final int CREATORS_LIMIT = 3;

    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;
    private final RestTemplate restTemplate;
    private final BaseMusicRepository baseMusicRepo;
    private final MotionMusicRepository motionMusicRepo;
    private final MusicSummaryRepository summaryRepo;
    private final MotionMusicRepository motionMusicRepository;
    private final MotionMusicLikeRepository motionMusicLikeRepository;
    private final BaseMusicLikeRepository baseMusicLikeRepository;

    /** 외부 썸네일 생성기 (동영상 → 썸네일) */
    private final MotionCoverClient motionClient;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${app.s3.thumbs-prefix}")
    private String thumbsPrefix;

    /** FastAPI: 기본음악 생성/재생성에만 사용 */
    @Value("${fastapi.endpoint.generate}")
    private String fastApiUrl;

    /** "나의 노래 N" 기본 제목 생성 */
    private String generateDefaultTitle(Integer userId) {
        long count = baseMusicRepo.countByUserId(userId);
        return "나의 노래 " + (count + 1);
    }

    /** "나의 모션음악 N" 기본 제목 생성 */
    private String generateDefaultMotionTitle(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        long count = motionMusicRepo.findByUser(user).size();
        return "나의 모션음악 " + (count + 1);
    }

    /**
     * 프롬프트(=요약) + 선택적 허밍 → FastAPI → S3 업로드 → URL 반환
     * 허밍 파일은 선택(null/empty 허용).
     */
    public String generateMusicAndUpload(String prompt, @Nullable MultipartFile hummingFile) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("prompt", prompt);

        if (hummingFile != null && !hummingFile.isEmpty()) {
            body.add("file", new MultipartInputStreamFileResource(hummingFile));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, request, byte[].class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("🎵 FastAPI 음악 생성 실패");
        }

        byte[] audio = response.getBody();
        String key = "music/" + UUID.randomUUID() + ".wav";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(audio.length);
        metadata.setContentType("audio/wav");

        try (InputStream inputStream = new ByteArrayInputStream(audio)) {
            amazonS3.putObject(bucketName, key, inputStream, metadata);
        }

        return amazonS3.getUrl(bucketName, key).toString();
    }

    /**
     * 기존 기본음악 파일 + (sessionId의 최신 요약)으로 재생성 → 새 BaseMusic 저장
     * - sessionId로 기존 BaseMusic(삭제되지 않은 것)을 찾아, 그 파일을 FastAPI에 보내 재생성
     */
    @Transactional
    public MusicRegenerateResponse regenerateFromSummaryText(String sessionId, Integer userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        BaseMusic baseMusic = baseMusicRepo.findBySessionIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new RuntimeException("삭제되지 않은 기본 음악을 찾을 수 없습니다."));

        String prompt = summaryRepo.findBySessionId(sessionId)
                .map(MusicSummary::getSummaryText)
                .orElseThrow(() -> new RuntimeException("GPT 요약 없음"));

        // 기존 기본음악 오디오 가져오기
        String originalFileUrl = baseMusic.getFileUrl();
        String key = originalFileUrl.substring(originalFileUrl.indexOf("music/")); // "music/xxx.wav"

        S3Object s3Object = amazonS3.getObject(bucketName, key);
        byte[] audioBytes;
        try (InputStream inputStream = s3Object.getObjectContent()) {
            audioBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("기존 음악 S3 다운로드 실패", e);
        }

        // FastAPI에 기존 오디오 + prompt(=요약) 전달
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("prompt", prompt);
        body.add("file", new ByteArrayResource(audioBytes) {
            @Override public String getFilename() { return "input.wav"; }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, request, byte[].class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("FastAPI 응답 실패");
        }

        // 새 오디오 S3 업로드
        byte[] newAudio = response.getBody();
        String newKey = "music/" + UUID.randomUUID() + ".wav";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(newAudio.length);
        metadata.setContentType("audio/wav");

        try (InputStream stream = new ByteArrayInputStream(newAudio)) {
            amazonS3.putObject(bucketName, newKey, stream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        String fileUrl = amazonS3.getUrl(bucketName, newKey).toString();
        String resolvedTitle = (title == null || title.trim().isEmpty()) ? generateDefaultTitle(userId) : title;

        BaseMusic music = new BaseMusic();
        music.setSessionId(sessionId);
        music.setUser(user);
        music.setTitle(resolvedTitle);
        music.setFileUrl(fileUrl);
        music.setIsDeleted(false);

        baseMusicRepo.save(music);

        return new MusicRegenerateResponse(music.getId(), fileUrl, resolvedTitle);
    }

    /**
     * ✅ 기본음악 → 모션음악 생성 (오디오 생성 안 함)
     * - BaseMusic 메타만 복사하여 MotionMusic 한 건 생성
     * - count=0, visibility=false
     * - title 미지정 시 "나의 모션음악 N"
     */
    @Transactional
    public MotionMusicRegenerateResponse regenerateMotionMusic(Integer baseMusicId) {
        BaseMusic baseMusic = baseMusicRepo.findByIdAndIsDeletedFalse(baseMusicId)
                .orElseThrow(() -> new RuntimeException("삭제되지 않은 기본 음악이 존재하지 않습니다."));

        User owner = baseMusic.getUser();

        // 기본 제목 부여
        String motionTitle = generateDefaultMotionTitle(owner.getId());

        // 새 모션 레코드 생성 (오디오 새 생성 X)
        MotionMusic motionMusic = new MotionMusic();
        motionMusic.setBaseMusic(baseMusic);
        motionMusic.setUser(owner);
        motionMusic.setSessionId(baseMusic.getSessionId());
        motionMusic.setTitle(motionTitle);
        motionMusic.setFileUrl(baseMusic.getFileUrl()); // 기본음악 오디오 그대로 사용
        motionMusic.setCount(0);                        // 조회수 0
        motionMusic.setVisibility(false);               // 기본 비공개
        motionMusicRepo.save(motionMusic);

        return new MotionMusicRegenerateResponse(
                motionMusic.getId(),
                motionMusic.getFileUrl(),
                baseMusic.getTitle(),
                motionTitle
        );
    }

    /** 프론트 업로드 영상 key를 모션음악에 부착 + 썸네일 생성 */
    @Transactional
    public void attachMotionVideoAndCover(Integer motionMusicId, String inputKey, Double timestampSec) {
        MotionMusic mm = motionMusicRepo.findById(motionMusicId)
                .orElseThrow(() -> new RuntimeException("MotionMusic not found: " + motionMusicId));

        Map<String, Object> res = motionClient.generateThumbnailFromS3(
                bucketName, inputKey, timestampSec, bucketName, thumbsPrefix
        );
        String thumbUrl = (String) res.get("thumbnail_url");
        if (thumbUrl == null || thumbUrl.isBlank()) {
            throw new RuntimeException("썸네일 URL이 비어 있습니다.");
        }

        String videoUrl = amazonS3.getUrl(bucketName, inputKey).toString();
        mm.setAniUrl(videoUrl);
        mm.setCover(thumbUrl);

        motionMusicRepo.save(mm);
    }

    // 음악 상세 페이지
    @Transactional
    public MusicDetailResponseDTO getDetail(Integer musicId, String context, Integer viewerIdOrNull) {
        MotionMusic motionMusic = motionMusicRepository.findByIdWithOwnerAndBase(musicId)
                .orElseThrow(() -> new CustomException(MusicErrorCode.MUSIC_NOT_FOUND));
        motionMusic.setCount(motionMusic.getCount() + 1);

        User owner = motionMusic.getUser();
        BaseMusic baseMusic = motionMusic.getBaseMusic();

        boolean isOwner = viewerIdOrNull != null && owner.getId().equals(viewerIdOrNull);
        if ("explore".equals(context) && !motionMusic.getVisibility() && !isOwner) {
            throw new CustomException(MusicErrorCode.NO_PERMISSION);
        }

        // 좋아요/북마크 (로그인 안 했으면 false)
        boolean isLiked = viewerIdOrNull != null
                && motionMusicLikeRepository.existsByUserIdAndMotionMusicId(viewerIdOrNull, motionMusic.getId());

        boolean isBookmarkedBaseMusic = viewerIdOrNull != null
                && baseMusicLikeRepository.existsByUserIdAndBaseMusicId(viewerIdOrNull, baseMusic.getId());

        MusicDetailResponseDTO.ViewerState viewerState = MusicDetailResponseDTO.ViewerState.builder()
                .isOwner(isOwner)
                .isLiked(isLiked)
                .isBookmarkedBaseMusic(isBookmarkedBaseMusic)
                .build();

        MusicDetailResponseDTO.MusicLite musicLite = MusicDetailResponseDTO.MusicLite.builder()
                .id(motionMusic.getId())
                .title(motionMusic.getTitle())
                .artist(owner.getNickname())
                .artistProfileImage(owner.getProfileImage())
                .coverImageUrl(motionMusic.getCover())
                .fileUrl(motionMusic.getAniUrl())
                .viewCount(motionMusic.getCount())
                .likeCount(motionMusic.getLikes() != null ? motionMusic.getLikes().size() : 0)
                .build();

        MusicDetailResponseDTO.BaseMusicLite baseLite = MusicDetailResponseDTO.BaseMusicLite.builder()
                .id(baseMusic.getId())
                .artist(baseMusic.getUser().getNickname())
                .title(baseMusic.getTitle())
                .fileUrl(baseMusic.getFileUrl())
                .build();

        MusicDetailResponseDTO dto = MusicDetailResponseDTO.builder()
                .music(musicLite)
                .usedBaseMusic(baseLite)
                .viewerState(viewerState)
                .build();

        if ("mypage".equals(context)) {
            // 로그인 필수: 전체 프로필 제공
            User viewer = userRepository.findById(viewerIdOrNull)
                    .orElseThrow(() -> new CustomException(GeneralErrorCode.INVALID_TOKEN));
            dto.setViewerProfile(UserProfileDTO.builder()
                    .id(viewer.getId())
                    .nickname(viewer.getNickname())
                    .providerType(viewer.getProviderType())
                    .email(viewer.getEmail())
                    .profileImage(viewer.getProfileImage())
                    .build());
        } else if ("explore".equals(context) && viewerIdOrNull != null) {
            // 로그인 상태면 이미지 한 장만
            userRepository.findById(viewerIdOrNull)
                    .map(User::getProfileImage)
                    .ifPresent(dto::setViewerProfileImage);
        }

        if ("explore".equals(context)) {
            // creatorsUsingBase: 공개 트랙 작성자 프로필 3개
            List<String> profiles = motionMusicRepository.findDistinctCreatorProfileImagesByBase(
                    baseMusic.getId(), PageRequest.of(0, CREATORS_LIMIT));
            dto.setCreatorsUsingBase(
                    MusicDetailResponseDTO.CreatorsUsingBase.builder()
                            .profileImages(profiles)
                            .build()
            );

            // related 6개: 로그인 여부와 무관하게 공개곡 기준으로 선별
            List<MusicDetailResponseDTO.RelatedLite> related = buildRelatedForExplore(motionMusic.getId(), baseMusic.getId());
            dto.setRelated(related);
        }

        return dto;
    }

    private List<MusicDetailResponseDTO.RelatedLite> buildRelatedForExplore(Integer motionId, Integer baseId) {
        // 1순위: 같은 baseMusic
        var primaryViews = motionMusicRepository.findRelatedPrimary(motionId, baseId, PageRequest.of(0, RELATED_LIMIT));
        List<MusicDetailResponseDTO.RelatedLite> related = new ArrayList<>(RELATED_LIMIT);
        Set<Integer> seen = new HashSet<>();

        for (var v : primaryViews) {
            if (seen.add(v.getId())) related.add(map(v));
            if (related.size() == RELATED_LIMIT) return related;
        }

        // 2순위: 다른 baseMusic 보충
        int remain = RELATED_LIMIT - related.size();
        if (remain > 0) {
            var fallbackViews = motionMusicRepository.findRelatedFallback(motionId, baseId, PageRequest.of(0, remain * 2));
            for (var v : fallbackViews) { // 혹시 모를 중복 대비 여유분 가져와서 채움
                if (seen.add(v.getId())) related.add(map(v));
                if (related.size() == RELATED_LIMIT) break;
            }
        }
        return related;
    }

    private static MusicDetailResponseDTO.RelatedLite map(RelatedItemView v) {
        return MusicDetailResponseDTO.RelatedLite.builder()
                .id(v.getId())
                .title(v.getTitle())
                .artist(v.getArtist())
                .coverImageUrl(v.getCoverImageUrl())
                .viewCount(v.getViewCount())
                .build();
    }
}
