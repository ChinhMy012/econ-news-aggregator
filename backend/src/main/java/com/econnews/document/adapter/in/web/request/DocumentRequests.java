package com.econnews.document.adapter.in.web.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class DocumentRequests {

    private DocumentRequests() {}

    public record Create(
        @NotBlank @Size(max = 300) String title,
        @NotBlank @Size(max = 200) String issuer,
        @NotBlank @Size(max = 2048) String url,
        LocalDate issuedOn,
        @NotNull Boolean enabled
    ) {}

    public record Update(
        @NotBlank @Size(max = 300) String title,
        @NotBlank @Size(max = 200) String issuer,
        LocalDate issuedOn,
        @NotNull Boolean enabled,
        @NotNull @PositiveOrZero Long version
    ) {}
}
