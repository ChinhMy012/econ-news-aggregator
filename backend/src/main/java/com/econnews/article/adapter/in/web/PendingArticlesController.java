package com.econnews.article.adapter.in.web;

import com.econnews.article.application.ListPendingArticles;
import com.econnews.article.application.port.ArticleQuery;
import com.econnews.article.domain.model.ArticlePreview;
import com.econnews.shared.adapter.web.response.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/articles")
public class PendingArticlesController {

    private final ListPendingArticles list;

    public PendingArticlesController(ArticleQuery query) {
        this.list = new ListPendingArticles(query);
    }

    public record ArticleResponse(
        long id,
        long sourceId,
        long firstFeedId,
        String title,
        String originalUrl,
        String summary,
        Instant publishedAt,
        Instant collectedAt,
        String status
    ) {
        public static ArticleResponse from(ArticlePreview a) {
            return new ArticleResponse(
                a.id(),
                a.sourceId(),
                a.firstFeedId(),
                a.title(),
                a.originalUrl(),
                a.summary(),
                a.publishedAt(),
                a.collectedAt(),
                a.status()
            );
        }
    }

    @GetMapping
    public ResponseEntity<?> pending(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        var result = list.list(page, size);
        return ApiResponses.page(
            "Bài viết chờ duyệt.",
            result.items().stream().map(ArticleResponse::from).toList(),
            PageMeta.of(page, size, result.total()),
            request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalid(IllegalArgumentException e, HttpServletRequest request) {
        return ApiResponses.error(
            HttpStatus.BAD_REQUEST,
            "INVALID_PAGINATION",
            e.getMessage(),
            List.of(),
            request
        );
    }
}
