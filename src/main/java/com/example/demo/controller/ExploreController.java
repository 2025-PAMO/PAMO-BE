package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.domain.MotionMusic;
import com.example.demo.dto.explore.ExploreResponseDTO;
import com.example.demo.dto.explore.MusicDebugDTO;
import com.example.demo.repository.MotionMusicRepository;
import com.example.demo.service.ExploreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/explore")
@RequiredArgsConstructor
public class ExploreController {
    private final ExploreService exploreService;
    private final MotionMusicRepository motionMusicRepository;

    @GetMapping
    public CustomResponse<ExploreResponseDTO> showExplore(@RequestParam(required = false) Integer id) {
        if (id != null) {
            return CustomResponse.onSuccess(exploreService.getExplorePage(id));
        } else {
            return CustomResponse.onSuccess(exploreService.getExplorePage());
        }
    }

    // Explore 테스트용 컨트롤러
    @GetMapping("/dev/music-order")
    public List<MusicDebugDTO> debugMusicOrder() {
        return motionMusicRepository.findAll().stream()
                .filter(MotionMusic::getVisibility) // 공개된 음악만
                .sorted(Comparator.comparing(MotionMusic::getCreatedAt).reversed())
                .map(music -> new MusicDebugDTO(
                        music.getId(),
                        music.getTitle(),
                        music.getUser().getNickname(),
                        music.getCover(),
                        music.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }


}
