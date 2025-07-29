package com.example.demo.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RefreshTokenCookieManager {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    public static void clear(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
    }
}

