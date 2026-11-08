package com.econnews.auth.application.model;

public record LoginCommand(
        String username,
        String password) {

    @Override
    public String toString() {
        return "LoginCommand[REDACTED]";
    }
}
