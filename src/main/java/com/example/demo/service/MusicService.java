package com.example.demo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.example.demo.apiPayload.code.MusicErrorCode;
import com.example.demo.apiPayload.exception.CustomException;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.MusicSummary;
import com.example.demo.domain.User;
import com.example.demo.dto.music.MusicDetailResponseDTO;
import com.example.demo.dto.music.MusicRegenerateResponse;
import com.example.demo.dto.user.UserProfileDTO;
import com.example.demo.repository.*;
import com.example.demo.repository.projection.RelatedItemView;
import com.example.demo.util.S3Uploader;
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
    private final MotionMusicLikeRepository motionMusicLikeRepository;
    private final BaseMusicLikeRepository baseMusicLikeRepository;
    private final MotionCoverClient motionClient;
    private final S3Uploader s3Uploader;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${fastapi.endpoint.generate}")
    private String fastApiUrl;

    // ✅ 기본 음악 생성 (FastAPI 호출 후 S3 업로드)
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

    // ✅ 기본 음악 재생성
    @Transactional
    public MusicRegenerateResponse regenerateFromSummaryText(String sessionId, Integer userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        BaseMusic baseMusic = baseMusicRepo.findBySessionIdAndDeletableFalse(sessionId)
                .orElseThrow(() -> new RuntimeException("삭제되지 않은 기본 음악을 찾을 수 없습니다."));

        String prompt = summaryRepo.findBySessionId(sessionId)
                .map(MusicSummary::getSummaryText)
                .orElseThrow(() -> new RuntimeException("GPT 요약 없음"));

        String originalFileUrl = baseMusic.getFileUrl();
        String key = originalFileUrl.substring(originalFileUrl.indexOf("music/"));

        S3Object s3Object = amazonS3.getObject(bucketName, key);
        byte[] audioBytes;
        try (InputStream inputStream = s3Object.getObjectContent()) {
            audioBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("기존 음악 S3 다운로드 실패", e);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("prompt", prompt);
        body.add("file", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return "input.wav";
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, request, byte[].class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("FastAPI 응답 실패");
        }

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
        String resolvedTitle = (title == null || title.trim().isEmpty())
                ? "나의 노래 " + (baseMusicRepo.countByUserId(userId) + 1)
                : title;

        BaseMusic music = new BaseMusic();
        music.setSessionId(sessionId);
        music.setUser(user);
        music.setTitle(resolvedTitle);
        music.setFileUrl(fileUrl);
        music.setDeletable(false);
        baseMusicRepo.save(music);

        return new MusicRegenerateResponse(music.getId(), fileUrl, resolvedTitle);
    }

    // ✅ 모션 비디오 업로드 (썸네일 생성 포함)
    @Transactional
    public Map<String, Object> uploadMotionVideo(Integer userId, Integer baseId, MultipartFile file, String title) throws IOException {
        BaseMusic baseMusic = baseMusicRepo.findByIdAndDeletableFalse(baseId)
                .orElseThrow(() -> new RuntimeException("기본 음악을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 1️⃣ 비디오 업로드
        String videoUrl = s3Uploader.upload(file, "motion-video");

        // 2️⃣ 실제 S3 Key 추출 (URL → Key)
        String fileKey = videoUrl.substring(videoUrl.indexOf("motion-video/"));

        // 3️⃣ FastAPI 호출로 썸네일 생성
        Map<String, Object> thumbResult = motionClient.generateThumbnailFromS3(
                bucketName,
                fileKey,       // ✅ 실제 S3 key 전달
                1.5,           // 썸네일 추출 타임 (초)
                bucketName,
                "motion-thumbs"
        );

        String coverUrl = thumbResult != null ? (String) thumbResult.get("thumbnail_url") : null;

        // 4️⃣ MotionMusic DB 저장
        MotionMusic motionMusic = new MotionMusic();
        motionMusic.setUser(user);
        motionMusic.setBaseMusic(baseMusic);
        motionMusic.setTitle(title != null ? title : "나의 모션뮤직비디오");
        motionMusic.setAniUrl(videoUrl);
        motionMusic.setFileUrl(baseMusic.getFileUrl());
        motionMusic.setCover(coverUrl);
        motionMusic.setVisibility(false);
        motionMusicRepo.save(motionMusic);

        // 5️⃣ 응답 반환
        Map<String, Object> result = new HashMap<>();
        result.put("motionMusicId", motionMusic.getId());
        result.put("fileUrl", videoUrl);
        result.put("coverUrl", coverUrl);
        result.put("baseTitle", baseMusic.getTitle());
        result.put("motionTitle", motionMusic.getTitle());
        return result;
    }

    // ✅ 수정된 오디오 업로드 (프론트에서 만든 오디오)
    @Transactional
    public Map<String, Object> uploadMotionAudio(Integer userId, Integer baseId, MultipartFile audioFile, String title) throws IOException {
        BaseMusic baseMusic = baseMusicRepo.findByIdAndDeletableFalse(baseId)
                .orElseThrow(() -> new RuntimeException("기본 음악을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String audioUrl = s3Uploader.upload(audioFile, "motion-music");

        MotionMusic motionMusic = new MotionMusic();
        motionMusic.setUser(user);
        motionMusic.setBaseMusic(baseMusic);
        motionMusic.setTitle(title != null ? title : "나의 모션음악");
        motionMusic.setFileUrl(audioUrl);
        motionMusic.setVisibility(false);
        motionMusicRepo.save(motionMusic);

        Map<String, Object> result = new HashMap<>();
        result.put("motionMusicId", motionMusic.getId());
        result.put("fileUrl", audioUrl);
        result.put("baseTitle", baseMusic.getTitle());
        result.put("motionTitle", motionMusic.getTitle());

        return result;
    }

    // ✅ 음악 상세 조회
    @Transactional
    public MusicDetailResponseDTO getDetail(Integer musicId, String context, Integer viewerIdOrNull) {
        MotionMusic motionMusic = motionMusicRepo.findByIdWithOwnerAndBase(musicId)
                .orElseThrow(() -> new CustomException(MusicErrorCode.MUSIC_NOT_FOUND));
        motionMusic.setCount(motionMusic.getCount() + 1);

        User owner = motionMusic.getUser();
        BaseMusic baseMusic = motionMusic.getBaseMusic();

        boolean isOwner = viewerIdOrNull != null && owner.getId().equals(viewerIdOrNull);
        if ("explore".equals(context) && !motionMusic.getVisibility() && !isOwner) {
            throw new CustomException(MusicErrorCode.NO_PERMISSION);
        }

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

        return MusicDetailResponseDTO.builder()
                .music(musicLite)
                .usedBaseMusic(baseLite)
                .viewerState(viewerState)
                .build();
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
