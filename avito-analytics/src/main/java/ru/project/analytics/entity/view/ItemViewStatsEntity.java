package ru.project.analytics.entity.view;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ItemViewStatsEntity(
        long itemId,

        long totalViews,

        long uniqueViews,

        Instant lastViewedAt,

        long viewsLastSevenDays
) {
}
