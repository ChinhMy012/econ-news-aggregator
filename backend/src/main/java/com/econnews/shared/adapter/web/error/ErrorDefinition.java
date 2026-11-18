package com.econnews.shared.adapter.web.error;

import org.springframework.http.HttpStatus;

public record ErrorDefinition(HttpStatus status, String code, String message) {}
