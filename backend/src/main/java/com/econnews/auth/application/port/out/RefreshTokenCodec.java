package com.econnews.auth.application.port.out;

public interface RefreshTokenCodec {
    String generate();
    String hash(String rawToken);
    boolean isWellFormed(String rawToken);
}
