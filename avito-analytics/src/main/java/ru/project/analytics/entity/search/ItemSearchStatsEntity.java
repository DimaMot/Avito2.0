package ru.project.analytics.entity.search;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ItemSearchStatsEntity(
        long id,

        String query,

        long totalViews,

        Integer resultsCount,

        long uniqueViews,

        Instant lastSearchAt
) {
}