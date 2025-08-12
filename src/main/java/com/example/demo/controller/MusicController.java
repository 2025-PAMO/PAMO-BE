package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MusicSummary;
import com.example.demo.domain.User;
import com.example.demo.dto.music.MusicRegenerateRequest;
import com.example.demo.dto.music.MusicRegenerateResponse;
import com.example.demo.dto.music.MotionMusicRegenerateResponse;
import com.example.demo.oauth.entity.UserPrincipal;
import com.example.demo.repository.BaseMusicRepository;
import com.example.demo.repository.MusicSummaryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final BaseMusicRepository baseMusicRepo;
    private final MusicSummaryRepository summaryRepo;
    private final UserRepository userRepository;
    private final MusicService musicService;

    /** 기본음악 생성: prompt는 받지 않음. file(허밍)은 선택. */
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<Map<String, Object>>> generateMusic(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("sessionId") String sessionId,
            @RequestParam(value = "title", required = false) String title,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        Integer userId = userPrincipal.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        String summaryText = summaryRepo.findBySessionId(sessionId)
                .map(MusicSummary::getSummaryText)
                .orElseThrow(() -> new RuntimeException("GPT 요약 없음"));

        String resolvedTitle = (title == null || title.isBlank())
                ? "나의 노래 " + (baseMusicRepo.countByUserId(userId) + 1)
                : title;

        String fileUrl = musicService.generateMusicAndUpload(summaryText, file);

        BaseMusic music = new BaseMusic();
        music.setSessionId(sessionId);
        music.setUser(user);
        music.setTitle(resolvedTitle);
        music.setFileUrl(fileUrl);
        music.setIsDeleted(false);
        baseMusicRepo.save(music);

        Map<String, Object> response = new HashMap<>();
        response.put("musicId", music.getId());
        response.put("summary", summaryText);
        response.put("fileUrl", music.getFileUrl());

        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }

    /** 기본음악 재생성: sessionId의 요약으로 재생성 */
    @PostMapping(value = "/regenerate/from-summary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomResponse<MusicRegenerateResponse>> regenerateFromSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody MusicRegenerateRequest request) {

        MusicRegenerateResponse response = musicService.regenerateFromSummaryText(
                request.getSessionId(),
                userPrincipal.getId(),
                request.getTitle()
        );
        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }

    /** ✅ 기본음악 → 모션음악 생성 (쿼리 파라미터로 baseId 받기) */
    @PostMapping("/motion/regenerate")
    public ResponseEntity<CustomResponse<MotionMusicRegenerateResponse>> regenerateMotionMusic(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Integer baseId) {

        MotionMusicRegenerateResponse response = musicService.regenerateMotionMusic(baseId);
        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }

    /** 프론트 업로드 영상 key를 모션음악에 부착 + 썸네일 생성 (기존 유지) */
    @PostMapping("/motion/attach")
    public ResponseEntity<CustomResponse<Void>> attachMotionAssets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Integer motionMusicId,
            @RequestParam String inputKey,
            @RequestParam(required = false) Double timestampSec
    ) {
        musicService.attachMotionVideoAndCover(motionMusicId, inputKey, timestampSec);
        return ResponseEntity.ok(CustomResponse.onSuccess(null));
    }
}
