package com.example.demo.controller;


import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.myMusic.MyMusicResponseDTO;
import com.example.demo.service.MyMusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyMusicService myMusicService;

    @GetMapping("/musics")
    public CustomResponse<MyMusicResponseDTO> getMyMusic(
            @RequestParam Integer id,
            @RequestParam String type
    ) {
        MyMusicResponseDTO result = myMusicService.getMyMusic(id, type);
        return CustomResponse.onSuccess(result);
    }

}
