package com.econnews.source.application.port;

import java.util.function.Supplier;

public interface CatalogTransaction {
    <T> T execute(Supplier<T> work);
}
