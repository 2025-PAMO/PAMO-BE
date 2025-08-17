package com.example.demo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.MusicSummary;
import com.example.demo.domain.User;
import com.example.demo.dto.music.MotionMusicRegenerateResponse;
import com.example.demo.dto.music.MusicRegenerateResponse;
import com.example.demo.repository.BaseMusicRepository;
import com.example.demo.repository.MotionMusicRepository;
import com.example.demo.repository.MusicSummaryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;
    private final RestTemplate restTemplate;
    private final BaseMusicRepository baseMusicRepo;
    private final MotionMusicRepository motionMusicRepo;
    private final MusicSummaryRepository summaryRepo;

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
     */
    @Transactional
    public MusicRegenerateResponse regenerateFromSummaryText(String sessionId, Integer userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        BaseMusic baseMusic = baseMusicRepo.findBySessionIdAndDeletableFalse(sessionId)
                .orElseThrow(() -> new RuntimeException("삭제되지 않은 기본 음악을 찾을 수 없습니다."));

        String prompt = summaryRepo.findBySessionId(sessionId)
                .map(MusicSummary::getSummaryText)
                .orElseThrow(() -> new RuntimeException("GPT 요약 없음"));

        // 기존 기본음악 오디오 가져오기
        String originalFileUrl = baseMusic.getFileUrl();
        String key = originalFileUrl.substring(originalFileUrl.indexOf("music/"));

        S3Object s3Object = amazonS3.getObject(bucketName, key);
        byte[] audioBytes;
        try (InputStream inputStream = s3Object.getObjectContent()) {
            audioBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("기존 음악 S3 다운로드 실패", e);
        }

        // FastAPI에 기존 오디오 + prompt 전달
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
        music.setDeletable(false);

        baseMusicRepo.save(music);

        return new MusicRegenerateResponse(music.getId(), fileUrl, resolvedTitle);
    }

    /** 기본음악 → 모션음악 생성 */
    @Transactional
    public MotionMusicRegenerateResponse regenerateMotionMusic(Integer baseMusicId) {
        BaseMusic baseMusic = baseMusicRepo.findByIdAndDeletableFalse(baseMusicId)
                .orElseThrow(() -> new RuntimeException("삭제되지 않은 기본 음악이 존재하지 않습니다."));

        User owner = baseMusic.getUser();

        String motionTitle = generateDefaultMotionTitle(owner.getId());

        MotionMusic motionMusic = new MotionMusic();
        motionMusic.setBaseMusic(baseMusic);
        motionMusic.setUser(owner);
        motionMusic.setSessionId(baseMusic.getSessionId());
        motionMusic.setTitle(motionTitle);
        motionMusic.setFileUrl(baseMusic.getFileUrl());
        motionMusic.setCount(0);
        motionMusic.setVisibility(false);
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
}
