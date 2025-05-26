package com.example.demo.Controller;

import com.example.demo.DTO.MusicGenerateRequest;
import com.example.demo.Entity.BaseMusic;
import com.example.demo.Entity.MusicSummary;
import com.example.demo.Repository.BaseMusicRepository;
import com.example.demo.Repository.MusicSummaryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/music")
public class MusicController {

    private final BaseMusicRepository baseMusicRepo;
    private final MusicSummaryRepository summaryRepo;

    public MusicController(BaseMusicRepository baseMusicRepo, MusicSummaryRepository summaryRepo) {
        this.baseMusicRepo = baseMusicRepo;
        this.summaryRepo = summaryRepo;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateMusic(@RequestBody MusicGenerateRequest request) {
        String sessionId = request.getSessionId();
        Integer userId = request.getUserId();
        String title = request.getTitle();
        String fileUrl = request.getFileUrl();

        // 1. GPT 요약 조회
        MusicSummary summary = summaryRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("GPT 요약 없음"));

        // 2. 기본 음악 저장
        BaseMusic music = new BaseMusic();
        music.setSessionId(sessionId);
        music.setUserId(userId);
        music.setTitle(title);
        music.setFileUrl(fileUrl);

        baseMusicRepo.save(music);

        // 3. 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("musicId", music.getId());
        response.put("summary", summary.getSummaryText());
        response.put("fileUrl", music.getFileUrl());

        return ResponseEntity.ok(response);
    }
}
