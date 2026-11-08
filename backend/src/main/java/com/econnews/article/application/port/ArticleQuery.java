package com.econnews.article.application.port;

import com.econnews.article.domain.model.ArticlePreview;
import java.util.List;

public interface ArticleQuery {
    record Slice(List<ArticlePreview> items, long total) {}

    Slice pending(int page, int size);
}
