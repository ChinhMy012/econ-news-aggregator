package com.econnews.source.application.exception;

public class CatalogException extends RuntimeException {

    private final CatalogError error;

    public CatalogException(CatalogError error) {
        super(error.name());
        this.error = error;
    }

    public CatalogError error() {
        return error;
    }
}
