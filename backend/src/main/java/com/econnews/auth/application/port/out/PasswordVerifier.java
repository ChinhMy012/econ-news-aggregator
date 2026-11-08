package com.econnews.auth.application.port.out;

public interface PasswordVerifier {

    /**
     * Nếu passwordHash là null, thực hiện kiểm tra giả
     * để giảm khác biệt thời gian rồi trả false.
     */
    boolean matches(String rawPassword, String passwordHash);
}