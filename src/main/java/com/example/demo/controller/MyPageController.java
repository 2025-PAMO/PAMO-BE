package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.myPage.TitleUpdateRequestDTO;
import com.example.demo.dto.myPage.VisibilityUpdateRequestDTO;
import com.example.demo.dto.myPage.MyMusicResponseDTO;
import com.example.demo.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.util.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/musics")
    public CustomResponse<MyMusicResponseDTO> getMyMusic(
            @RequestParam String type
    ) {
        Integer userId = getCurrentUserId();
        MyMusicResponseDTO result = myPageService.getMyMusic(userId, type);
        return CustomResponse.onSuccess(result);
    }

    @GetMapping("/library")
    public CustomResponse<MyMusicResponseDTO> getMyLibrary(
            @RequestParam String type
    ){
        Integer userId = getCurrentUserId();
        MyMusicResponseDTO result = myPageService.getMyLibrary(userId, type);
        return CustomResponse.onSuccess(result);
    }

    @PatchMapping("/motion-music/{id}/visibility")
    public CustomResponse<String> updateVisibility(
            @PathVariable("id") Integer musicId,
            @RequestBody VisibilityUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        myPageService.updateVisibility(userId, musicId, requestDTO.getVisibility());
        return CustomResponse.onSuccess("모션 음악 공개 여부가 성공적으로 변경되었습니다.");
    }

    @PatchMapping("/motion-music/{id}/title")
    public CustomResponse<String> updateMotionMusicTitle(
            @PathVariable("id") Integer musicId,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        myPageService.updateMotionMusicTitle(userId, musicId, requestDTO.getTitle());
        return CustomResponse.onSuccess("모션 음악 제목이 성공적으로 수정되었습니다.");
    }

    @PatchMapping("/base-music/{id}/title")
    public CustomResponse<String> updateBaseMusicTitle(
            @PathVariable("id") Integer musicId,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        Integer userId = getCurrentUserId();
        myPageService.updateBaseMusicTitle(userId, musicId, requestDTO.getTitle());
        return CustomResponse.onSuccess("기본 음악 제목이 성공적으로 수정되었습니다.");
    }

}
