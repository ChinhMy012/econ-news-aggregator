package com.econnews.collection.application.port;

import java.util.function.Supplier;

public interface CollectionTransaction {
    <T> T execute(Supplier<T> work);
}
