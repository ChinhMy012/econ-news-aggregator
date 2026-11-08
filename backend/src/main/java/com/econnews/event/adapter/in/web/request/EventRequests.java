package com.econnews.event.adapter.in.web.request;

import com.econnews.event.application.model.EventCommands.*;
import jakarta.validation.constraints.*;
import java.util.List;

public final class EventRequests {

    private EventRequests() {}

    public record Create(
        @NotBlank @Size(max = 300) String title,
        @NotBlank String contentType,
        @NotNull @Size(max = 10000) String overview
    ) {
        public EventDraft command() {
            return new EventDraft(title, contentType, overview);
        }
    }

    public record Update(
        @NotBlank @Size(max = 300) String title,
        @NotBlank String contentType,
        @NotNull @Size(max = 10000) String overview,
        @NotNull @PositiveOrZero Long version
    ) {
        public EventDraft command() {
            return new EventDraft(title, contentType, overview);
        }
    }

    public record Version(@NotNull @PositiveOrZero Long version) {}

    public record Hide(
        @NotBlank @Size(max = 1000) String reason,
        @NotNull @PositiveOrZero Long version
    ) {}

    public record Sources(
        @NotNull @Size(max = 50) List<@NotNull @Positive Long> articleIds,
        @NotNull @Size(max = 50) List<@NotNull @Positive Long> documentIds,
        @NotNull @PositiveOrZero Long version
    ) {}

    public record MilestoneBody(
        @NotBlank @Size(max = 300) String title,
        @NotNull @Size(max = 4000) String description,
        @NotBlank String datePrecision,
        @NotNull @Min(1) @Max(9999) Integer dateYear,
        @Min(1) @Max(12) Integer dateMonth,
        @Min(1) @Max(31) Integer dateDay,
        @NotNull @PositiveOrZero Long version
    ) {
        public MilestoneDraft command() {
            return new MilestoneDraft(
                title,
                description,
                datePrecision,
                dateYear,
                dateMonth,
                dateDay
            );
        }
    }
}
