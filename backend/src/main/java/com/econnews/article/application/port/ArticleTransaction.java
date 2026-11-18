package com.econnews.article.application.port;

import java.util.function.Supplier;

public interface ArticleTransaction {
    <T> T execute(Supplier<T> work);
}
