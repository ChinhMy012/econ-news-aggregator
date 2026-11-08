package com.econnews.shared.application.port;

import java.util.function.Supplier;

public interface ContentTransaction {
    <T> T write(Supplier<T> work);
    <T> T read(Supplier<T> work);
}
