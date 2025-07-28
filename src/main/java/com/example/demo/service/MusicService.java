package com.example.demo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MusicSummary;
import com.example.demo.domain.MotionMusic;
import com.example.demo.domain.User;
import com.example.demo.dto.music.MotionMusicRegenerateResponse;
import com.example.demo.dto.music.MusicRegenerateResponse;
import com.example.demo.repository.BaseMusicRepository;
import com.example.demo.repository.MusicSummaryRepository;
import com.example.demo.repository.MotionMusicRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MusicService {

    @Autowired
    private UserRepository userRepository;

    private final AmazonS3 amazonS3;
    private final RestTemplate restTemplate;
    private final BaseMusicRepository baseMusicRepo;
    private final MusicSummaryRepository summaryRepo;
    private final MotionMusicRepository motionMusicRepo;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${fastapi.endpoint.generate}")
    private String fastApiUrl;

    /**
     * 프롬프트 + 허밍파일로 기본 음악 생성 & S3 업로드
     */
    public String generateMusicAndUpload(String prompt, MultipartFile hummingFile) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("prompt", prompt);
        body.add("file", new MultipartInputStreamFileResource(hummingFile));

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
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        return amazonS3.getUrl(bucketName, key).toString();
    }

    /**
     * 기존 기본 음악(BaseMusic) 기반으로 모션 음악 재생성
     */
    public MotionMusicRegenerateResponse regenerateMotionMusic(Integer baseMusicId) {
        BaseMusic baseMusic = baseMusicRepo.findById(baseMusicId)
                .orElseThrow(() -> new RuntimeException("기본 음악이 존재하지 않습니다."));

        MusicSummary summary = summaryRepo.findBySessionId(baseMusic.getSessionId())
                .orElseThrow(() -> new RuntimeException("GPT 요약 없음"));

        String prompt = summary.getSummaryText();
        String originalFileUrl = baseMusic.getFileUrl();
        String key = originalFileUrl.substring(originalFileUrl.indexOf("music/")); // music/xxx.wav

        // S3에서 파일 다운로드
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        InputStream inputStream = s3Object.getObjectContent();
        byte[] audioBytes;
        try {
            audioBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 다운로드 실패", e);
        }

        // FastAPI 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("prompt", prompt);
        body.add("file", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return "input.wav";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, request, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("FastAPI 응답 실패");
        }

        // S3 업로드
        byte[] newAudio = response.getBody();
        String newKey = "motion/" + UUID.randomUUID() + ".wav";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(newAudio.length);
        metadata.setContentType("audio/wav");

        try (InputStream uploadStream = new ByteArrayInputStream(newAudio)) {
            amazonS3.putObject(bucketName, newKey, uploadStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("모션 음악 S3 업로드 실패", e);
        }

        String newFileUrl = amazonS3.getUrl(bucketName, newKey).toString();

        // DB 저장
        MotionMusic motionMusic = new MotionMusic();
        motionMusic.setBaseMusic(baseMusic);
        motionMusic.setFileUrl(newFileUrl);
        motionMusicRepo.save(motionMusic);

        return new MotionMusicRegenerateResponse(motionMusic.getId(), newFileUrl);
    }

    /**
     * 프롬프트 기반으로 기본 음악 처음부터 다시 생성
     */
    public MusicRegenerateResponse regenerateFromPromptOnly(String sessionId, Integer userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        MusicSummary summary = summaryRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("GPT 요약 없음"));

        String prompt = summary.getSummaryText();

        // FastAPI 호출
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("prompt", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(fastApiUrl, HttpMethod.POST, request, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("FastAPI 응답 실패");
        }

        byte[] newAudio = response.getBody();
        String key = "music/" + UUID.randomUUID() + ".wav";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(newAudio.length);
        metadata.setContentType("audio/wav");

        try (InputStream stream = new ByteArrayInputStream(newAudio)) {
            amazonS3.putObject(bucketName, key, stream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        String fileUrl = amazonS3.getUrl(bucketName, key).toString();

        BaseMusic music = new BaseMusic();
        music.setSessionId(sessionId);
        music.setUser(user);
        music.setTitle(title);
        music.setFileUrl(fileUrl);

        baseMusicRepo.save(music);

        return new MusicRegenerateResponse(music.getId(), fileUrl, title);
    }
}
