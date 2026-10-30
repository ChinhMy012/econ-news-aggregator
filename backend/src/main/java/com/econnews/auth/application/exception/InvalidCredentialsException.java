package com.econnews.auth.application.exception;

//vì lỗi này cần trả 401, còn handler nghiệp vụ hiện trả 400.
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Tên đăng nhập hoặc mật khẩu không đúng.");
    }
}