package com.econnews.shared.adapter.web.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        boolean success,
        int statusCode,
        String errorCode,
        String message,
        List<FieldViolation> errors,
        Instant timestamp,
        String requestId) {
}
