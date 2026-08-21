package ru.project.avito.item.dto;

import java.time.OffsetDateTime;

public record CommentDto(
        Long id,
        String text,
        String authorName,
        OffsetDateTime created
) {}
