package com.example.demo.controller;

import com.example.demo.dto.summary.MusicGenerateRequest;
import com.example.demo.dto.summary.SummaryDTO;
import com.example.demo.dto.summary.SummaryRequest;
import com.example.demo.domain.MusicSummary;
import com.example.demo.converter.SummaryConverter;
import com.example.demo.repository.MusicSummaryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/summary")
public class SummaryController {

    private final MusicSummaryRepository summaryRepo;

    public SummaryController(MusicSummaryRepository summaryRepo) {
        this.summaryRepo = summaryRepo;
    }

    // ✅ 요약 저장 API
    @PostMapping
    public ResponseEntity<?> saveSummary(@RequestBody SummaryRequest request) {
        if (summaryRepo.existsBySessionId(request.getSessionId())) {
            return ResponseEntity.badRequest().body("이미 존재하는 session_id입니다.");
        }

        MusicSummary summary = new MusicSummary();
        summary.setSessionId(request.getSessionId());
        summary.setSummaryText(request.getSummaryText());

        summaryRepo.save(summary);
        return ResponseEntity.ok("요약 저장 완료");
    }

    // ✅ 요약 조회 API
    @PostMapping("/music/generate")
    public ResponseEntity<?> generate(@RequestBody MusicGenerateRequest request) {
        MusicSummary entity = summaryRepo.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("요약 없음"));

        SummaryDTO dto = SummaryConverter.toDTO(entity);

        Map<String, Object> response = new HashMap<>();
        response.put("summaryText", dto.getSummaryText());
        response.put("sessionId", dto.getSessionId());

        return ResponseEntity.ok(response);
    }
}
