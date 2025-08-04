package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.myPage.TitleUpdateRequestDTO;
import com.example.demo.dto.myPage.VisibilityUpdateRequestDTO;
import com.example.demo.dto.myPage.MyMusicResponseDTO;
import com.example.demo.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/musics")
    public CustomResponse<MyMusicResponseDTO> getMyMusic(
            @RequestParam Integer id,
            @RequestParam String type
    ) {
        MyMusicResponseDTO result = myPageService.getMyMusic(id, type);
        return CustomResponse.onSuccess(result);
    }

    @GetMapping("/library")
    public CustomResponse<MyMusicResponseDTO> getMyLibrary(
            @RequestParam Integer id,
            @RequestParam String type
    ){
        MyMusicResponseDTO result = myPageService.getMyLibrary(id, type);
        return CustomResponse.onSuccess(result);
    }

    @PatchMapping("/motion-music/{id}/visibility")
    public CustomResponse<String> updateVisibility(
            @PathVariable("id") Integer id,
            @RequestBody VisibilityUpdateRequestDTO requestDTO
            ){
        myPageService.updateVisibility(id, requestDTO.getVisibility());
        return CustomResponse.onSuccess("모션 음악 공개 여부가 성공적으로 변경되었습니다.");
    }

    @PatchMapping("/motion-music/{id}/title")
    public CustomResponse<String> updateMotionMusicTitle(
            @PathVariable("id") Integer id,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        myPageService.updateMotionMusicTitle(id, requestDTO.getTitle());
        return CustomResponse.onSuccess("모션 음악 제목이 성공적으로 수정되었습니다.");
    }

    @PatchMapping("/base-music/{id}/title")
    public CustomResponse<String> updateBaseMusicTitle(
            @PathVariable("id") Integer id,
            @RequestBody TitleUpdateRequestDTO requestDTO
    ){
        myPageService.updateBaseMusicTitle(id, requestDTO.getTitle());
        return CustomResponse.onSuccess("기본 음악 제목이 성공적으로 수정되었습니다.");
    }

}
