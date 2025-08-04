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

    public AuthToken(Integer id, String userId, String role, Date expiry, Key key) {
        this.key = key;
        this.token = Jwts.builder()
                .setSubject(userId)
                .claim("id", id)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    public boolean validate() {
        try {
            getTokenClaims();
            System.out.println("[JWT 유효성 통과] 토큰: " + token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("[JWT 유효성 실패] 만료됨: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("[JWT 유효성 실패] 지원 안됨: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("[JWT 유효성 실패] 형식 오류: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("[JWT 유효성 실패] 서명 오류(secret 불일치): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[JWT 유효성 실패] 기타: " + e.getMessage());
        }
        return false;
    }

//    public boolean validate() {
//        try {
//            getTokenClaims();
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }

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

