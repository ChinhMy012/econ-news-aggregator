package com.econnews.article.adapter.in.web;

import com.econnews.article.application.exception.ArticleException;
import com.econnews.shared.adapter.web.response.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(
    assignableTypes = { ArticleAdminController.class, PublicArticlesController.class }
)
@Order(-1)
public class ArticleExceptionHandler {

    @ExceptionHandler(ArticleException.class)
    public ResponseEntity<?> handle(ArticleException e, HttpServletRequest request) {
        return ApiResponses.error(
            HttpStatusCode.valueOf(e.status()),
            e.code(),
            e.getMessage(),
            List.of(),
            request
        );
    }
}
