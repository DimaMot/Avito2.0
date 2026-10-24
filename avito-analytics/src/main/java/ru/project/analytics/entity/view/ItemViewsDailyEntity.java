package ru.project.analytics.entity.view;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ItemViewsDailyEntity(
        long id,

        long itemId,

        LocalDate day,

        long views
) {
}
