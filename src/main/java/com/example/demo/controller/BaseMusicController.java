package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.music.TitleUpdateRequestDTO;
import com.example.demo.service.BaseMusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.util.SecurityUtil.getCurrentUserId;

@Tag(name = "기본 음악 API", description = "기본 음악 리소스 관리 API입니다. 제목 수정, 삭제, 북마크 추가/해제를 제공합니다.")
@RestController
@RequestMapping("/api/base-music")
@RequiredArgsConstructor
public class BaseMusicController {
    private final BaseMusicService baseMusicService;

    @Operation(summary = "기본 음악 제목 수정", description = "기본 음악의 제목을 변경합니다.")
    @PatchMapping("/{id}/title")
    public CustomResponse<String> updateBaseMusicTitle(
            @PathVariable("id") Integer musicId,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        baseMusicService.updateBaseMusicTitle(userId, musicId, requestDTO.getTitle());
        return CustomResponse.onSuccess("기본 음악 제목이 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "기본 음악 삭제", description = "사용자가 소유한 기본 음악을 삭제합니다.")
    @DeleteMapping("{id}")
    public CustomResponse<String> deleteBaseMusic(@PathVariable("id") Integer musicId) {
        Integer userId = getCurrentUserId();
        baseMusicService.deleteBaseMusic(userId, musicId);
        return CustomResponse.onSuccess("기본 음악이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "기본 음악 북마크 추가", description = "기본 음악을 내 라이브러리에 북마크합니다.")
    @PutMapping("/{id}/bookmark")
    public CustomResponse<String> bookmark(@PathVariable("id") Integer musicId) {
        Integer userId = getCurrentUserId();
        baseMusicService.bookmark(userId, musicId);
        return CustomResponse.onSuccess("기본 음악을 북마크에 추가했습니다.");
    }

    @Operation(summary = "기본 음악 북마크 해제", description = "기본 음악을 내 라이브러리 북마크에서 제거합니다.")
    @DeleteMapping("/{id}/bookmark")
    public CustomResponse<String> unbookmark(@PathVariable("id") Integer musicId) {
        Integer userId = getCurrentUserId();
        baseMusicService.unbookmark(userId, musicId);
        return CustomResponse.onSuccess("기본 음악 북마크가 해제되었습니다.");
    }

}
