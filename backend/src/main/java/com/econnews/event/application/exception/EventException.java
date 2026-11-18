package com.econnews.event.application.exception;

public class EventException extends RuntimeException {

    private final EventError error;
    private final String field;

    public EventException(EventError error) {
        this(error, null);
    }

    public EventException(EventError error, String field) {
        super(error.name());
        this.error = error;
        this.field = field;
    }

    public EventError error() {
        return error;
    }

    public String field() {
        return field;
    }
}
