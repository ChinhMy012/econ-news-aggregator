package com.econnews.article.application.exception;

public class ArticleException extends RuntimeException {

    private final ArticleError error;

    public ArticleException(ArticleError error) {
        super(error.name());
        this.error = error;
    }

    public ArticleError error() {
        return error;
    }
}
