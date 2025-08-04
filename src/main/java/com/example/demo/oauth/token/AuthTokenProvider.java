package com.example.demo.oauth.token;

import com.example.demo.oauth.entity.RoleType;
import com.example.demo.oauth.entity.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthTokenProvider {


    @Value("${app.auth.tokenSecret}")

    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public AuthToken createAuthToken(Integer id, String userId, String role, Date expiry) {
        return new AuthToken(id, userId, role, expiry, key);
    }

    public AuthToken createAuthToken(String token) {
        return new AuthToken(token, key);
    }

    public Authentication getAuthentication(AuthToken authToken) {
        Claims claims = authToken.getTokenClaims();
        //Integer id = claims.get("id", Integer.class);

        Object idObj = claims.get("id");


        System.out.println("[디버깅] claim에서 꺼낸 id: " + idObj);
        Integer id = null;
        if (idObj instanceof Integer) {
            id = (Integer) idObj;
        } else if (idObj instanceof String) {
            id = Integer.valueOf((String) idObj); // 이거 아니면 파싱 오류로 401
        }

        String userId = claims.getSubject();
        RoleType role = RoleType.of(claims.get("role", String.class));

        UserPrincipal userPrincipal = UserPrincipal.of(id, userId, role);
        System.out.println("[디버깅] 만든 Principal: " + userPrincipal);
        return new UsernamePasswordAuthenticationToken(userPrincipal, authToken, userPrincipal.getAuthorities());
    }

    public boolean validateToken(AuthToken authToken) {
        return authToken.validate();
    }
}
