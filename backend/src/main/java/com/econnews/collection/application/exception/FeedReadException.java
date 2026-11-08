package com.econnews.collection.application.exception;

public class FeedReadException extends RuntimeException {

    private final String code;

    public FeedReadException(String code) {
        super(code);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
