package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.user.NicknameRequestDTO;
import com.example.demo.dto.myPage.MyMusicResponseDTO;
import com.example.demo.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.example.demo.util.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;
    private final UserService userService;

    @Operation(summary = "내가 생성한 음악 조회 API", description = "현재 대화데이터를 바탕으로 요약을 생성합니다.")
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

    @PatchMapping("/profile-image")
    public CustomResponse<String> updateProfileImage(@RequestParam("profileImage") MultipartFile profileImage) throws IOException {
        Integer userId = getCurrentUserId();
        userService.updateProfileImage(userId, profileImage);
        return CustomResponse.onSuccess("사용자의 프로필 사진이 성공적으로 수정되었습니다.");
    }

    @PatchMapping("/nickname")
    public CustomResponse<String> updateNickname(@RequestBody NicknameRequestDTO requestDTO) {
        Integer userId = getCurrentUserId();
        userService.updateNickname(userId, requestDTO.getNickname());
        return CustomResponse.onSuccess("사용자의 닉네임이 성공적으로 수정되었습니다.");
    }

}
