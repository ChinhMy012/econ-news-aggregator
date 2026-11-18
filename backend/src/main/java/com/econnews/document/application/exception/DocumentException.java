package com.econnews.document.application.exception;

public class DocumentException extends RuntimeException {

    private final DocumentError error;

    public DocumentException(DocumentError error) {
        super(error.name());
        this.error = error;
    }

    public DocumentError error() {
        return error;
    }
}
