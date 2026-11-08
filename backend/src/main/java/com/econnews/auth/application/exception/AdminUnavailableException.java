package com.econnews.auth.application.exception;

public class AdminUnavailableException extends RuntimeException {
    public AdminUnavailableException() {
        super("Tài khoản không còn khả dụng. Vui lòng đăng nhập lại.");
    }
}
