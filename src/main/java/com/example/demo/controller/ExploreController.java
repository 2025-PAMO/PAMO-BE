package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.explore.ExploreResponseDTO;
import com.example.demo.oauth.entity.UserPrincipal;
import com.example.demo.repository.MotionMusicRepository;
import com.example.demo.service.ExploreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "홈화면 API", description = "홈화면에 보여질 내용을 조회합니다.")
@RestController
@RequestMapping("/explore")
@RequiredArgsConstructor
public class ExploreController {
    private final ExploreService exploreService;
    private final MotionMusicRepository motionMusicRepository;

    @Operation(summary = "홈화면/둘러보기 조회 API")
    @GetMapping
    public CustomResponse<ExploreResponseDTO> showExplore() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return CustomResponse.onSuccess(exploreService.getExplorePage(userPrincipal.getId()));
        } else {
            return CustomResponse.onSuccess(exploreService.getExplorePage());
        }
    }

}
