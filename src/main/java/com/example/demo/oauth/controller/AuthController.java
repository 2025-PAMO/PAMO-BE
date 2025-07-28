package com.example.demo.oauth.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.oauth.config.AppProperties;
import com.example.demo.oauth.domain.UserRefreshToken;
import com.example.demo.oauth.token.AuthToken;
import com.example.demo.oauth.token.AuthTokenProvider;

import com.example.demo.repository.UserRefreshTokenRepository;
import com.example.demo.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final UserRefreshTokenRepository userRefreshTokenRepository;

    @GetMapping("/refresh")
    public CustomResponse<String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenStr = CookieUtil.getCookie(request, "refresh_token")
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshTokenStr == null) {
            return CustomResponse.onFailure("NO_REFRESH_TOKEN", "Refresh Token not found", null);
        }

        AuthToken refreshToken = tokenProvider.createAuthToken(refreshTokenStr);

        if (!refreshToken.validate()) {
            return CustomResponse.onFailure("INVALID_TOKEN", "Invalid Refresh Token", null);
        }

        String userId = refreshToken.getSubject();
        UserRefreshToken savedToken = userRefreshTokenRepository.findByUserId(userId);

        if (savedToken == null || !savedToken.getRefreshToken().equals(refreshTokenStr)) {
            return CustomResponse.onFailure("MISMATCH_TOKEN", "Token mismatch", null);
        }

        Date now = new Date();
        long accessTokenExpiry = appProperties.getAuth().getTokenExpiry();

        AuthToken newAccessToken = tokenProvider.createAuthToken(
                userId,
                "ROLE_USER", // 또는 principal.getRoleType().getCode() 등
                new Date(now.getTime() + accessTokenExpiry)
        );

        return CustomResponse.onSuccess(newAccessToken.getToken());
    }
}
