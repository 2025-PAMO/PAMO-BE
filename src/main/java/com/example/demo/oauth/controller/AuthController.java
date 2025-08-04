package com.example.demo.oauth.controller;

import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.domain.User;
import com.example.demo.oauth.config.AppProperties;
import com.example.demo.oauth.domain.UserRefreshToken;
import com.example.demo.oauth.token.AuthToken;
import com.example.demo.oauth.token.AuthTokenProvider;

import com.example.demo.repository.UserRefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRepository userRepository;

    @GetMapping("/refresh")
    public CustomResponse<String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenStr = CookieUtil.getCookie(request, "refresh_token")
                .map(Cookie::getValue)
                .orElse(null);
        System.out.println("[🔍 로그] 추출된 쿠키 값 (Encoded): " + refreshTokenStr);

        if (refreshTokenStr == null) {
            System.out.println("[❌ 로그] 쿠키에서 refresh_token 없음");
            return CustomResponse.onFailure("NO_REFRESH_TOKEN", "Refresh Token not found", null);
        }
        String decodedTokenStr = URLDecoder.decode(refreshTokenStr, StandardCharsets.UTF_8);

        AuthToken refreshToken = tokenProvider.createAuthToken(decodedTokenStr);

        if (!refreshToken.validate()) {
            System.out.println("[❌ 로그] 토큰 유효성 검사 실패");
            return CustomResponse.onFailure("INVALID_TOKEN", "Invalid Refresh Token", null);
        }

        String userId = refreshToken.getSubject();
        System.out.println("[🔍 로그] 토큰에서 추출된 userId: " + userId);
        User user = userRepository.findByUserId(userId);
        UserRefreshToken savedToken = userRefreshTokenRepository.findByUserId(userId);

        if (savedToken == null || !savedToken.getRefreshToken().equals(refreshTokenStr)) {
            return CustomResponse.onFailure("MISMATCH_TOKEN", "Token mismatch", null);
        }

        Date now = new Date();
        long accessTokenExpiry = appProperties.getAuth().getTokenExpiry();

        AuthToken newAccessToken = tokenProvider.createAuthToken(
                user.getId(),
                userId,
                "ROLE_USER", // 또는 principal.getRoleType().getCode() 등
                new Date(now.getTime() + accessTokenExpiry)
        );

        return CustomResponse.onSuccess(newAccessToken.getToken());
    }
}
