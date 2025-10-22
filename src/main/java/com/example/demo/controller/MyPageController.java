package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.user.NicknameRequestDTO;
import com.example.demo.dto.myPage.MyMusicResponseDTO;
import com.example.demo.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.example.demo.util.SecurityUtil.getCurrentUserId;

@Tag(name = "마이페이지 API", description = "내 음악/라이브러리 조회 및 프로필 관리 API")
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;
    private final UserService userService;

    @Operation(summary = "내 음악 목록 조회", description = "사용자가 생성한 음악을 type별로 조회합니다. type은 motion, base 중 하나여야 합니다.")
    @GetMapping("/musics")
    public CustomResponse<MyMusicResponseDTO> getMyMusic(
            @RequestParam String type
    ) {
        Integer userId = getCurrentUserId();
        MyMusicResponseDTO result = myPageService.getMyMusic(userId, type);
        return CustomResponse.onSuccess(result);
    }

    @Operation(summary = "내 라이브러리 조회", description = "좋아요/북마크한 음악을 type별로 조회합니다. type은 motion, base 중 하나여야 합니다.")
    @GetMapping("/library")
    public CustomResponse<MyMusicResponseDTO> getMyLibrary(
            @RequestParam String type
    ){
        Integer userId = getCurrentUserId();
        MyMusicResponseDTO result = myPageService.getMyLibrary(userId, type);
        return CustomResponse.onSuccess(result);
    }

    @Operation(summary = "프로필 이미지 수정", description = "사용자의 프로필 이미지를 변경합니다.")
    @PatchMapping("/profile-image")
    public CustomResponse<String> updateProfileImage(@RequestParam("profileImage") MultipartFile profileImage) throws IOException {
        Integer userId = getCurrentUserId();
        userService.updateProfileImage(userId, profileImage);
        return CustomResponse.onSuccess("사용자의 프로필 사진이 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "닉네임 수정", description = "사용자의 닉네임을 변경합니다.")
    @PatchMapping("/nickname")
    public CustomResponse<String> updateNickname(@RequestBody NicknameRequestDTO requestDTO) {
        Integer userId = getCurrentUserId();
        userService.updateNickname(userId, requestDTO.getNickname());
        return CustomResponse.onSuccess("사용자의 닉네임이 성공적으로 수정되었습니다.");
    }

}
