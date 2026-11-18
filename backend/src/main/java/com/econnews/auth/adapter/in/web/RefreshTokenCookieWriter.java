package com.econnews.auth.adapter.in.web;

import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import com.econnews.bootstrap.config.AuthSessionProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RefreshTokenCookieWriter {
    public static final String NAME = "econnews_refresh";
    private final AuthSessionProperties properties;
    public RefreshTokenCookieWriter(AuthSessionProperties properties) { this.properties = properties; }
    public String read(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (NAME.equals(cookie.getName())) {
                if (token != null) return null; // Từ chối cookie trùng tên không rõ nguồn.
                token = cookie.getValue();
            }
        }
        return token;
    }
    public void write(HttpServletResponse response, String rawToken, Instant expiresAt) {
        long seconds = Math.max(0, Duration.between(Instant.now(), expiresAt).getSeconds());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(rawToken, seconds).toString());
    }
    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", 0).toString());
    }
    private ResponseCookie cookie(String value, long maxAge) {
        return ResponseCookie.from(NAME, value).httpOnly(true).secure(properties.cookieSecure())
                .sameSite("Lax").path("/api/v1/auth").maxAge(maxAge).build();
    }
}
