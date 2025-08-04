package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.domain.BaseMusic;
import com.example.demo.domain.MusicSummary;
import com.example.demo.domain.User;
import com.example.demo.dto.music.MusicRegenerateRequest;
import com.example.demo.dto.music.MusicRegenerateResponse;
import com.example.demo.dto.music.MotionMusicRegenerateResponse;
import com.example.demo.repository.BaseMusicRepository;
import com.example.demo.repository.MusicSummaryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

    private final BaseMusicRepository baseMusicRepo;
    private final MusicSummaryRepository summaryRepo;
    private final UserRepository userRepository;
    private final MusicService musicService;

    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<Map<String, Object>>> generateMusic(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("prompt") String prompt,
            @RequestPart("file") MultipartFile file) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        String resolvedTitle = (title == null || title.isBlank())
                ? "나의 노래 " + (baseMusicRepo.countByUserId(userId) + 1)
                : title;

        String fileUrl = musicService.generateMusicAndUpload(prompt, file);

        MusicSummary summary = summaryRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("GPT 요약 없음"));

        BaseMusic music = new BaseMusic();
        music.setSessionId(sessionId);
        music.setUser(user);
        music.setTitle(resolvedTitle);
        music.setFileUrl(fileUrl);
        baseMusicRepo.save(music);

        Map<String, Object> response = new HashMap<>();
        response.put("musicId", music.getId());
        response.put("summary", summary.getSummaryText());
        response.put("fileUrl", music.getFileUrl());

        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }

    @PostMapping(value = "/regenerate/from-summary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomResponse<MusicRegenerateResponse>> regenerateFromSummary(
            @RequestBody MusicRegenerateRequest request) {

        MusicRegenerateResponse response = musicService.regenerateFromPromptOnly(
                request.getSessionId(),
                request.getUserId(),
                request.getPrompt(),
                request.getTitle()
        );
        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }

    @PostMapping("/{baseId}/motion/regenerate")
    public ResponseEntity<CustomResponse<MotionMusicRegenerateResponse>> regenerateMotionMusic(
            @PathVariable Integer baseId) {

        MotionMusicRegenerateResponse response = musicService.regenerateMotionMusic(baseId);
        return ResponseEntity.ok(CustomResponse.onSuccess(response));
    }
}
