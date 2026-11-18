package com.econnews.article.domain.repository;

import com.econnews.article.domain.model.CollectedArticle;

public interface CollectedArticleRepository {
    boolean insertIfNew(long sourceId, long feedId, CollectedArticle article);
}
