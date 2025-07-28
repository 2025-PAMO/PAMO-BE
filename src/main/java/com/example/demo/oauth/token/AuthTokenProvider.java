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


    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public AuthToken createAuthToken(String userId, String role, Date expiry) {
        return new AuthToken(userId, role, expiry, key);
    }

    public AuthToken createAuthToken(String token) {
        return new AuthToken(token, key);
    }

    public Authentication getAuthentication(AuthToken authToken) {
        Claims claims = authToken.getTokenClaims();
        String userId = claims.getSubject();
        RoleType role = RoleType.of(claims.get("role", String.class));

        UserPrincipal userPrincipal = UserPrincipal.of(userId, role);
        return new UsernamePasswordAuthenticationToken(userPrincipal, authToken, userPrincipal.getAuthorities());
    }

    public boolean validateToken(AuthToken authToken) {
        return authToken.validate();
    }
}
