package ru.project.analytics.entity.search;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ItemSearchStatsByCityEntity(
        long id,

        String query,

        Integer resultsCount,

        String city,

        long totalViews,

        Instant lastSearchAt
) {
}
