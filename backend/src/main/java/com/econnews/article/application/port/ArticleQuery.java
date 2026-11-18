package com.econnews.article.application.port;

import com.econnews.article.domain.model.*;
import java.util.List;
import java.util.Optional;

public interface ArticleQuery {
    record Slice(List<ArticleDetails> items, long total) {}

    Slice list(ArticleStatus status, ContentType contentType, int page, int size);
    Optional<ArticleDetails> find(long id);
}
