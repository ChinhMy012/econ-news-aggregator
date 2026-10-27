package com.econnews.shared.adapter.web.response;

import java.time.Instant;
import java.util.List;

public record ApiPageResponse<T>(
        boolean success,
        int statusCode,
        String message,
        List<T> data,
        PageMeta meta,
        Instant timestamp,
        String requestId) {
}