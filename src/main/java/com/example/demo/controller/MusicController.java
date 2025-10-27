package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MusicSummary;
import com.example.demo.domain.User;
import com.example.demo.dto.music.MusicRegenerateRequest;
import com.example.demo.dto.music.MusicRegenerateResponse;
import com.example.demo.oauth.entity.UserPrincipal;
import com.example.demo.repository.BaseMusicRepository;
import com.example.demo.repository.MusicSummaryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "음악 API", description = "기본음악 및 모션음악 업로드 관련 API")
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final BaseMusicRepository baseMusicRepo;
    private final MusicSummaryRepository summaryRepo;
    private final UserRepository userRepository;
    private final MusicService musicService;

    /**
     * ✅ 기본 음악 생성
     * 허밍데이터와 요약 텍스트를 기반으로 기본음악을 생성합니다.
     */
    @Operation(summary = "기본 음악 생성", description = "허밍데이터와 요약 텍스트를 기반으로 기본음악을 생성합니다.")
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
        music.setDeletable(false);
        baseMusicRepo.save(music);

        Map<String, Object> response = new HashMap<>();
        response.put("musicId", music.getId());
        response.put("summary", summaryText);
        response.put("fileUrl", music.getFileUrl());

        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }

    /**
     * ✅ 기본 음악 재생성
     * GPT 요약 텍스트를 바탕으로 새로운 기본음악을 생성합니다.
     */
    @Operation(summary = "음악 재생성", description = "요약 텍스트를 바탕으로 기본음악을 재생성합니다.")
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

    /**
     * ✅ 모션 비디오 업로드 및 모션음악 생성
     * 프론트에서 촬영된 모션 영상(mp4)을 업로드하고,
     * BaseMusic을 기반으로 FastAPI에 전달하여 모션용 음악을 생성합니다.
     */
    @Operation(summary = "모션 비디오 업로드", description = "프론트에서 촬영된 모션영상(mp4)을 업로드하고 BaseMusic과 연결합니다. (FastAPI 호출 포함)")
    @PostMapping(value = "/{baseId}/motion/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<Map<String, Object>>> uploadMotionVideo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer baseId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String effects // 🆕 추가됨
    ) throws IOException {

        Map<String, Object> result = musicService.uploadMotionVideo(
                userPrincipal.getId(),
                baseId,
                file,
                title,
                effects // ✅ 새 파라미터 전달
        );
        return ResponseEntity.ok(CustomResponse.onSuccess(result));
    }
}
