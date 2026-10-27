package com.econnews.shared.adapter.web.error;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.econnews.shared.adapter.web.logging.RequestIdFilter;
import com.econnews.shared.adapter.web.response.ApiResponses;
import com.econnews.shared.application.exception.BusinessRuleException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Object> handleBusinessRule(
            BusinessRuleException exception, WebRequest request) {
        return buildResponse(exception.getCode(), exception.getMessage(), List.of(),
                HttpStatus.BAD_REQUEST, HttpHeaders.EMPTY, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        List<FieldViolation> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
            errors.add(new FieldViolation(error.getField(), rule(error.getCode()),
                    error.getDefaultMessage())));
        exception.getBindingResult().getGlobalErrors().forEach(error ->
            errors.add(new FieldViolation(null, rule(error.getCode()),
                    error.getDefaultMessage())));
        return buildResponse("VALIDATION_FAILED", "Dữ liệu gửi lên không hợp lệ.",
                errors, status, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("HTTP error requestId={}", requestId(request), exception);
        }
        return buildResponse(errorCode(status), errorMessage(status), List.of(),
                status, headers, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception exception, WebRequest request) {
        log.error("Unexpected error requestId={}", requestId(request), exception);
        return buildResponse("INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.", List.of(),
                HttpStatus.INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY, request);
    }

    private ResponseEntity<Object> buildResponse(
            String code, String message, List<FieldViolation> errors,
            HttpStatusCode status, HttpHeaders headers, WebRequest request) {
        ServletWebRequest servletRequest = (ServletWebRequest) request;
        if (servletRequest.getResponse() != null && servletRequest.getResponse().isCommitted()) {
            return null;
        }
        ApiErrorResponse body = ApiResponses.errorBody(status, code, message,
                errors, servletRequest.getRequest());
        return new ResponseEntity<>(body, headers, status);
    }

    private String requestId(WebRequest request) {
        Object value = request.getAttribute(RequestIdFilter.ATTRIBUTE, WebRequest.SCOPE_REQUEST);
        return value == null ? null : value.toString();
    }

    private String rule(String constraint) {
        if (constraint == null) return null;
        return switch (constraint) {
            case "NotBlank" -> "NOT_BLANK";
            case "NotNull" -> "NOT_NULL";
            case "NotEmpty" -> "NOT_EMPTY";
            case "Email" -> "EMAIL_FORMAT";
            case "Size" -> "SIZE";
            case "Min", "DecimalMin" -> "MIN_VALUE";
            case "Max", "DecimalMax" -> "MAX_VALUE";
            case "Pattern" -> "PATTERN";
            case "Positive" -> "POSITIVE";
            case "PositiveOrZero" -> "POSITIVE_OR_ZERO";
            default -> "INVALID_VALUE";
        };
    }

    private String errorCode(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "RESOURCE_NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 406 -> "NOT_ACCEPTABLE";
            case 409 -> "CONFLICT";
            case 413 -> "PAYLOAD_TOO_LARGE";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            case 429 -> "TOO_MANY_REQUESTS";
            default -> status.is5xxServerError() ? "INTERNAL_SERVER_ERROR" : "HTTP_" + status.value();
        };
    }

    private String errorMessage(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Dữ liệu gửi lên không hợp lệ hoặc thiếu thông tin bắt buộc.";
            case 401 -> "Bạn cần đăng nhập để thực hiện yêu cầu.";
            case 403 -> "Bạn không được phép thực hiện yêu cầu.";
            case 404 -> "Không tìm thấy tài nguyên.";
            case 405 -> "Phương thức HTTP không được hỗ trợ.";
            case 406 -> "Định dạng phản hồi yêu cầu không được hỗ trợ.";
            case 409 -> "Yêu cầu xung đột với trạng thái hiện tại.";
            case 413 -> "Dữ liệu gửi lên vượt kích thước cho phép.";
            case 415 -> "Định dạng dữ liệu gửi lên không được hỗ trợ.";
            case 429 -> "Bạn gửi quá nhiều yêu cầu. Vui lòng thử lại sau.";
            default -> status.is5xxServerError()
                    ? "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."
                    : "Yêu cầu không hợp lệ hoặc không được hỗ trợ.";
        };
    }
}
