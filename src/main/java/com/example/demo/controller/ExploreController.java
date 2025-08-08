package com.example.demo.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.explore.ExploreResponseDTO;
import com.example.demo.oauth.entity.UserPrincipal;
import com.example.demo.service.ExploreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/explore")
@RequiredArgsConstructor
public class ExploreController {
    private final ExploreService exploreService;

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
