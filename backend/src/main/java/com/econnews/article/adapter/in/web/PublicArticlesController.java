package com.econnews.article.adapter.in.web;

import com.econnews.article.adapter.in.web.response.PublicArticleResponse;
import com.econnews.article.application.ManageArticles;
import com.econnews.shared.adapter.web.response.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/articles")
public class PublicArticlesController {

    private final ManageArticles articles;

    public PublicArticlesController(ManageArticles articles) {
        this.articles = articles;
    }

    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(required = false) String contentType,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        var result = articles.publishedList(contentType, page, size);
        return ApiResponses.page(
            "Bài viết đã công khai.",
            result.items().stream().map(PublicArticleResponse::from).toList(),
            PageMeta.of(page, size, result.total()),
            request
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            "Chi tiết bài viết công khai.",
            PublicArticleResponse.from(articles.publishedDetail(id)),
            request
        );
    }
}
