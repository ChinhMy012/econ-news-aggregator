package com.econnews.auth.application.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Phiên đăng nhập không còn hợp lệ. Vui lòng đăng nhập lại.");
    }
}
