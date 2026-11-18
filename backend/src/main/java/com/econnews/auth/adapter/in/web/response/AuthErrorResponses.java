package com.econnews.auth.adapter.in.web.response;

import com.econnews.shared.adapter.web.error.ErrorDefinition;
import org.springframework.http.HttpStatus;

public final class AuthErrorResponses {

    private AuthErrorResponses() {}

    public static final ErrorDefinition INVALID_CREDENTIALS = new ErrorDefinition(
        HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không đúng."
    );

    public static final ErrorDefinition ADMIN_UNAVAILABLE = new ErrorDefinition(
        HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Tài khoản không còn khả dụng. Vui lòng đăng nhập lại."
    );

    public static final ErrorDefinition INVALID_REFRESH_TOKEN = new ErrorDefinition(
        HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Phiên đăng nhập không còn hợp lệ. Vui lòng đăng nhập lại."
    );

    public static final ErrorDefinition AUTHENTICATION_REQUIRED = new ErrorDefinition(
        HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Bạn cần đăng nhập để thực hiện yêu cầu."
    );

    public static final ErrorDefinition INVALID_ACCESS_TOKEN = new ErrorDefinition(
        HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "Access token không hợp lệ."
    );

    public static final ErrorDefinition ACCESS_TOKEN_EXPIRED = new ErrorDefinition(
        HttpStatus.UNAUTHORIZED, "ACCESS_TOKEN_EXPIRED", "Access token đã hết hạn."
    );

    public static final ErrorDefinition CSRF_INVALID = new ErrorDefinition(
        HttpStatus.FORBIDDEN, "CSRF_INVALID", "Thông tin bảo vệ CSRF thiếu hoặc không hợp lệ."
    );

    public static final ErrorDefinition FORBIDDEN = new ErrorDefinition(
        HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không được phép thực hiện yêu cầu."
    );

    public static final ErrorDefinition LOGIN_RATE_LIMITED = new ErrorDefinition(
        HttpStatus.TOO_MANY_REQUESTS, "LOGIN_RATE_LIMITED", "Bạn gửi quá nhiều yêu cầu. Vui lòng thử lại sau."
    );

    public static final ErrorDefinition REFRESH_RATE_LIMITED = new ErrorDefinition(
        HttpStatus.TOO_MANY_REQUESTS, "REFRESH_RATE_LIMITED", "Bạn gửi quá nhiều yêu cầu. Vui lòng thử lại sau."
    );
}
