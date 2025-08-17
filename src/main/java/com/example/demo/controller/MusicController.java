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

@Tag(name = "음악 API", description = "생성한 요약을 바탕으로 기본음악을 생성합니다.")
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final BaseMusicRepository baseMusicRepo;
    private final MusicSummaryRepository summaryRepo;
    private final UserRepository userRepository;
    private final MusicService musicService;

    @Operation(summary = "음악 생성하기", description = "허밍데이터를 기반으로 한 데이터를 바탕으로 음악을 생성합니다.")
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

    @Operation(summary = "음악 재생성하기", description = "응답한 결과가 마음에 들지 않는경우 요약을 바탕으로 음악을 다시 생성합니다.")
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

    @Operation(summary = "모션음악 재생성하기", description = "모션음악을 재생성합니다.")
    @PostMapping("/{baseId}/motion/regenerate")
    public ResponseEntity<CustomResponse<MotionMusicRegenerateResponse>> regenerateMotionMusic(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Integer baseId) {

        MotionMusicRegenerateResponse response = musicService.regenerateMotionMusic(baseId);
        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }

    /** 프론트 업로드 영상 key를 모션음악에 부착 + 썸네일 생성 */
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
