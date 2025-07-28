package com.example.demo.oauth.token;
import io.jsonwebtoken.*;
import lombok.Getter;

import java.security.Key;
import java.util.Date;

@Getter
public class AuthToken {
    private final String token;
    private final Key key;

    public AuthToken(String token, Key key) {
        this.token = token;
        this.key = key;
    }

    public AuthToken(String userId, String role, Date expiry, Key key) {
        this.key = key;
        this.token = Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validate() {
        try {
            getTokenClaims();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getTokenClaims() {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String getSubject() {
        if (validate()) {
            return getTokenClaims().getSubject();
        }
        return null;
    }

}

