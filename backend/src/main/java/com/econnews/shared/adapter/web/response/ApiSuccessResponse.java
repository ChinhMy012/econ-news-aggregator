package com.econnews.shared.adapter.web.response;

import java.time.Instant;

public record ApiSuccessResponse<T>(
        boolean success,
        int statusCode,
        String message,
        T data,
        Instant timestamp,
        String requestId) {
}
