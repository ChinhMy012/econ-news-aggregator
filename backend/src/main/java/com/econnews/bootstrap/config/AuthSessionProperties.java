package com.econnews.bootstrap.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.auth")
public record AuthSessionProperties(
        @DefaultValue("7d") Duration refreshTokenTtl,
        @DefaultValue("7d") Duration expiredTokenRetention,
        @DefaultValue("true") boolean cookieSecure) {
    public AuthSessionProperties {
        if (refreshTokenTtl == null || refreshTokenTtl.compareTo(Duration.ofSeconds(1)) < 0
                || expiredTokenRetention == null || expiredTokenRetention.isNegative()) {
            throw new IllegalArgumentException("Thời hạn phiên hoặc lưu giữ token không hợp lệ.");
        }
    }
}
