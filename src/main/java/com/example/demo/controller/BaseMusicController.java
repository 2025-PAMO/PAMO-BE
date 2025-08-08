package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.music.TitleUpdateRequestDTO;
import com.example.demo.service.BaseMusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.util.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/base-music")
@RequiredArgsConstructor
public class BaseMusicController {
    private final BaseMusicService baseMusicService;

    @PatchMapping("/{id}/title")
    public CustomResponse<String> updateBaseMusicTitle(
            @PathVariable("id") Integer musicId,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        baseMusicService.updateBaseMusicTitle(userId, musicId, requestDTO.getTitle());
        return CustomResponse.onSuccess("기본 음악 제목이 성공적으로 수정되었습니다.");
    }
}
