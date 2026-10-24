package ru.project.commonlib.kafka;

import java.time.Instant;

public record ItemViewEvent(
        Long itemId,        // Какой товар посмотрели
        Long userId,        // Кто посмотрел (null, если неавторизованный гость)
        String city,        // Город
        Instant timestamp   // Время просмотра
) {
}
