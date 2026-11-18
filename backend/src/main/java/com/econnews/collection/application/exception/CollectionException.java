package com.econnews.collection.application.exception;

public class CollectionException extends RuntimeException {

    private final CollectionError error;

    public CollectionException(CollectionError error) {
        super(error.name());
        this.error = error;
    }

    public CollectionError error() {
        return error;
    }
}
