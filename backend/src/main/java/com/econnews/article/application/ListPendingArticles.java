package com.econnews.article.application;

import com.econnews.article.application.port.ArticleQuery;

public class ListPendingArticles {

    private final ArticleQuery query;

    public ListPendingArticles(ArticleQuery query) {
        this.query = query;
    }

    public ArticleQuery.Slice list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException(
            "page phải từ 0; size từ 1 đến 100."
        );
        return query.pending(page, size);
    }
}
