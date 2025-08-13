package com.example.demo.oauth.filter;

import com.example.demo.oauth.token.AuthToken;
import com.example.demo.oauth.token.AuthTokenProvider;

import com.example.demo.util.HeaderUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenStr = HeaderUtil.getAccessToken(request);

        log.info("요청 URI: {}", request.getRequestURI());
        log.info("[JWT 인증 필터] 추출된 토큰: {}", tokenStr);

        try {
            if (StringUtils.hasText(tokenStr)) {
                AuthToken token = tokenProvider.createAuthToken(tokenStr);
                if (token.validate()) {
                    log.info("[JWT 인증 필터] 토큰 유효성 검증 성공");
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("[JWT 인증 필터] 토큰 유효성 검증 실패!");
                }
            }
        } catch (Exception e) {
            log.error("[JWT 인증 필터] 인증 처리 중 예외 발생", e);
        }

        filterChain.doFilter(request, response);
    }
}
