package com.econnews.article.adapter.in.web;

import com.econnews.article.adapter.in.web.response.ArticleMessages;
import com.econnews.article.adapter.in.web.request.ArticleRequests.*;
import com.econnews.article.adapter.in.web.response.AdminArticleResponse;
import com.econnews.article.application.ManageArticles;
import com.econnews.shared.adapter.web.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/articles")
public class ArticleAdminController {

    private final ManageArticles articles;

    public ArticleAdminController(ManageArticles articles) {
        this.articles = articles;
    }

    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(defaultValue = "PENDING") String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        var result = articles.adminList(status, page, size);
        return ApiResponses.page(
            ArticleMessages.ADMIN_LIST,
            result.items().stream().map(AdminArticleResponse::from).toList(),
            PageMeta.of(page, size, result.total()),
            request
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable long id, HttpServletRequest request) {
        return ApiResponses.ok(
            ArticleMessages.ADMIN_DETAIL,
            AdminArticleResponse.from(articles.detail(id)),
            request
        );
    }

    @PatchMapping("/{id}/classification")
    public ResponseEntity<?> classify(
        @PathVariable long id,
        @Valid @RequestBody Classification body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            ArticleMessages.CLASSIFIED,
            AdminArticleResponse.from(articles.classify(id, body.contentType(), body.version())),
            request
        );
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(
        @PathVariable long id,
        @Valid @RequestBody Publish body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            ArticleMessages.PUBLISHED,
            AdminArticleResponse.from(articles.publish(id, body.version())),
            request
        );
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(
        @PathVariable long id,
        @Valid @RequestBody Reason body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            ArticleMessages.REJECTED,
            AdminArticleResponse.from(articles.reject(id, body.reason(), body.version())),
            request
        );
    }

    @PostMapping("/{id}/hide")
    public ResponseEntity<?> hide(
        @PathVariable long id,
        @Valid @RequestBody Reason body,
        HttpServletRequest request
    ) {
        return ApiResponses.ok(
            ArticleMessages.HIDDEN,
            AdminArticleResponse.from(articles.hide(id, body.reason(), body.version())),
            request
        );
    }
}
