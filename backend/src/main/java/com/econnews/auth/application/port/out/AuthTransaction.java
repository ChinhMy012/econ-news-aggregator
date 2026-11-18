package com.econnews.auth.application.port.out;

import java.util.function.Supplier;

public interface AuthTransaction {
    <T> T execute(Supplier<T> operation);
}
