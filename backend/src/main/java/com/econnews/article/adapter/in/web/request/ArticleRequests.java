package com.econnews.article.adapter.in.web.request;

import jakarta.validation.constraints.*;

public final class ArticleRequests {

    private ArticleRequests() {}

    public record Classification(
        @NotBlank(message = "Phân loại không được trống.") String contentType,
        @NotNull @PositiveOrZero Long version
    ) {}

    public record Publish(@NotNull @PositiveOrZero Long version) {}

    public record Reason(
        @NotBlank(message = "Vui lòng nhập lý do.") @Size(
            max = 1000,
            message = "Lý do tối đa 1000 ký tự."
        ) String reason,
        @NotNull @PositiveOrZero Long version
    ) {}
}
