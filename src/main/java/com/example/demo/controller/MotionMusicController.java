package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.music.TitleUpdateRequestDTO;
import com.example.demo.dto.music.VisibilityUpdateRequestDTO;
import com.example.demo.service.MotionMusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.util.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/motion-music")
@RequiredArgsConstructor
public class MotionMusicController {
    private final MotionMusicService motionMusicService;

    @PatchMapping("/{id}/visibility")
    public CustomResponse<String> updateVisibility(
            @PathVariable("id") Integer musicId,
            @RequestBody VisibilityUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        motionMusicService.updateVisibility(userId, musicId, requestDTO.getVisibility());
        return CustomResponse.onSuccess("모션 음악 공개 여부가 성공적으로 변경되었습니다.");
    }

    @PatchMapping("/{id}/title")
    public CustomResponse<String> updateMotionMusicTitle(
            @PathVariable("id") Integer musicId,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        motionMusicService.updateMotionMusicTitle(userId, musicId, requestDTO.getTitle());
        return CustomResponse.onSuccess("모션 음악 제목이 성공적으로 수정되었습니다.");
    }
}
