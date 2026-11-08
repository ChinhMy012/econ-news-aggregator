package com.econnews.document.application.exception;

public class DocumentException extends RuntimeException {

    private final int status;
    private final String code;

    public DocumentException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }
}
