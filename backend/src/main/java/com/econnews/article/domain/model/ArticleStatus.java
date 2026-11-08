package com.econnews.article.domain.model;

public enum ArticleStatus {
    PENDING,
    PUBLISHED,
    REJECTED,
    HIDDEN;

    public boolean canTransitionTo(ArticleStatus target) {
        return (
            (this == PENDING && (target == PUBLISHED || target == REJECTED)) ||
            (this == PUBLISHED && target == HIDDEN)
        );
    }
}
