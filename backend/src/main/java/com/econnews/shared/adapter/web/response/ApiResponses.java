package com.econnews.shared.adapter.web.response;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import com.econnews.shared.adapter.web.error.ApiErrorResponse;
import com.econnews.shared.adapter.web.error.FieldViolation;
import com.econnews.shared.adapter.web.logging.RequestIdFilter;

import jakarta.servlet.http.HttpServletRequest;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<ApiSuccessResponse<T>> ok(
            String message,
            T data,
            HttpServletRequest request) {
        return success(HttpStatus.OK, message, data, request);
    }

    public static <T> ResponseEntity<ApiSuccessResponse<T>> created(
            String message,
            T data,
            URI location,
            HttpServletRequest request) {
        return ResponseEntity.created(location)
                .body(successBody(
                        HttpStatus.CREATED,
                        message,
                        data,
                        request));
    }

    public static <T> ResponseEntity<ApiSuccessResponse<T>> accepted(
            String message,
            T data,
            HttpServletRequest request) {
        return success(HttpStatus.ACCEPTED, message, data, request);
    }

    public static <T> ResponseEntity<ApiPageResponse<T>> page(
            String message,
            List<T> data,
            PageMeta meta,
            HttpServletRequest request) {
        ApiPageResponse<T> body = new ApiPageResponse<>(
                true,
                HttpStatus.OK.value(),
                message,
                List.copyOf(data),
                meta,
                Instant.now(),
                requestId(request));

        return ResponseEntity.ok(body);
    }

    public static ApiErrorResponse errorBody(
            HttpStatusCode status,
            String errorCode,
            String message,
            List<FieldViolation> errors,
            HttpServletRequest request) {
        return new ApiErrorResponse(
                false,
                status.value(),
                errorCode,
                message,
                List.copyOf(errors),
                Instant.now(),
                requestId(request));
    }

    public static ResponseEntity<ApiErrorResponse> error(
            HttpStatusCode status,
            String errorCode,
            String message,
            List<FieldViolation> errors,
            HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(errorBody(
                        status,
                        errorCode,
                        message,
                        errors,
                        request));
    }

    private static <T> ResponseEntity<ApiSuccessResponse<T>> success(
            HttpStatus status,
            String message,
            T data,
            HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(successBody(status, message, data, request));
    }

    private static <T> ApiSuccessResponse<T> successBody(
            HttpStatus status,
            String message,
            T data,
            HttpServletRequest request) {
        return new ApiSuccessResponse<>(
                true,
                status.value(),
                message,
                data,
                Instant.now(),
                requestId(request));
    }

    private static String requestId(HttpServletRequest request) {
        Object value = request.getAttribute(
                RequestIdFilter.ATTRIBUTE);

        return value == null ? null : value.toString();
    }
}
