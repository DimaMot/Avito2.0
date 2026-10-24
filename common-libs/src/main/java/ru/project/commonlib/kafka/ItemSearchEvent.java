package ru.project.commonlib.kafka;

import java.time.Instant;

public record ItemSearchEvent(
        String queryText,   // Что искали
        Long userId,        // Кто искал (null, если не авторизован)
        String city,
        Integer resultsCount,   // Сколько предметов нашлось
        Instant timestamp
) {}
