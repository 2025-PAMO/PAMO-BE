package com.example.demo.oauth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum RoleType {
    USER("ROLE_USER", "일반 사용자 권한"),
    ADMIN("ROLE_ADMIN", "관리자 권한"),
    GUEST("GUEST", "게스트 권한");

    private final String code;
    private final String displayName;

    public static RoleType of(String authority) {
        return Arrays.stream(values())
                .filter(r -> r.code.equals(authority))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + authority));
    }

}

