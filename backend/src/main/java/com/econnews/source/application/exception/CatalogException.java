package com.econnews.source.application.exception;

public class CatalogException extends RuntimeException {

    private final int status;
    private final String code;

    public CatalogException(int status, String code, String message) {
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
