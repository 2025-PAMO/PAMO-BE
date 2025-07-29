package com.example.demo.oauth.handler;

import com.example.demo.oauth.config.AppProperties;
import com.example.demo.oauth.domain.UserRefreshToken;
import com.example.demo.oauth.entity.UserPrincipal;
import com.example.demo.oauth.token.AuthToken;
import com.example.demo.oauth.token.AuthTokenProvider;
import com.example.demo.repository.UserRefreshTokenRepository;
import com.example.demo.util.CookieUtil;
import com.example.demo.util.RefreshTokenCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final UserRefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        long accessTokenExpiry = appProperties.getAuth().getTokenExpiry();
        long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

        AuthToken accessToken = tokenProvider.createAuthToken(
                principal.getUserId(),
                principal.getRoleType().getCode(),
                new Date(now.getTime() + accessTokenExpiry)
        );

        AuthToken refreshToken = tokenProvider.createAuthToken(
                principal.getUserId(),
                principal.getRoleType().getCode(),
                new Date(now.getTime() + refreshTokenExpiry)
        );


        // refresh token 저장 or 갱신
        UserRefreshToken existingToken = refreshTokenRepository.findByUserId(principal.getUserId());
        if (existingToken == null) {
            refreshTokenRepository.save(new UserRefreshToken(principal.getUserId(), refreshToken.getToken()));
        } else {
            existingToken.setRefreshToken(refreshToken.getToken());
        }

        // 쿠키에 refresh token 저장
        CookieUtil.addCookie(response, "refresh_token", refreshToken.getToken(), (int) (refreshTokenExpiry / 60));

        // access token 응답 + 리다이렉트 (프론트로 token 쿼리 전달)
        response.sendRedirect("/login/success?token=" + accessToken.getToken());

        // 인증 요청 관련 쿠키 제거
        RefreshTokenCookieManager.clear(request, response);
    }
}
