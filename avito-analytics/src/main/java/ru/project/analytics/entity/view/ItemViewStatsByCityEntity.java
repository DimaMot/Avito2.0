package ru.project.analytics.entity.view;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ItemViewStatsByCityEntity(
        long id,

        long itemId,

        String city,

        long totalViews,

        Instant lastViewedAt
) {
}
