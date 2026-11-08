package com.econnews.bootstrap.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.auth.rate-limit")
public record AuthRateLimitProperties(@DefaultValue("10") int loginRequests,
        @DefaultValue("60") int refreshRequests, @DefaultValue("1m") Duration window,
        @DefaultValue("10000") int maxClients) {
    public AuthRateLimitProperties {
        if (loginRequests < 1 || refreshRequests < 1 || maxClients < 1
                || window == null || window.toMillis() < 1000) {
            throw new IllegalArgumentException("Cấu hình giới hạn yêu cầu auth không hợp lệ.");
        }
    }
}
