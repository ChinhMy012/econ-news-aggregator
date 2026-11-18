package com.econnews.source.adapter.in.web.request;

import jakarta.validation.constraints.*;

public final class CatalogRequests {

    private CatalogRequests() {}

    public record CreateSource(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 2048) String websiteUrl,
        @NotNull Boolean enabled
    ) {}

    public record UpdateSource(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 2048) String websiteUrl,
        @NotNull Boolean enabled,
        @NotNull @PositiveOrZero Long version
    ) {}

    public record CreateFeed(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 2048) String url,
        @NotNull Boolean enabled
    ) {}

    public record UpdateFeed(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 2048) String url,
        @NotNull Boolean enabled,
        @NotNull @PositiveOrZero Long version
    ) {}
}
