package com.econnews.article.domain.repository;

import com.econnews.article.domain.model.*;

public interface ArticleModerationRepository {
    boolean update(
        long id,
        long expectedVersion,
        ArticleStatus expectedStatus,
        ArticleStatus status,
        ContentType contentType,
        String reason
    );
}
