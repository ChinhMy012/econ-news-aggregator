package com.econnews.auth.application.model;

import java.time.Instant;

// Kết quả cấp token

/*
value: chuỗi JWT.
expiresIn: thời hạn tính bằng giây.
expiresAt: thời điểm hết hạn.
Không để toString() làm lộ token.
*/
public record IssuedAccessToken(
        String value,
        long expiresIn,
        Instant expiresAt) {

    @Override
    public String toString() {
        return "IssuedAccessToken[REDACTED]";
    }
}