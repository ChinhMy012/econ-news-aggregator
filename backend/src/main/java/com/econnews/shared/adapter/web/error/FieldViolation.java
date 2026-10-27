package com.econnews.shared.adapter.web.error;

public record FieldViolation(
        String field,
        String rule,
        String message) {
}