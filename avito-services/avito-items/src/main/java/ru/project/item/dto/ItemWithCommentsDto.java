package ru.project.item.dto;

import java.util.List;

public record ItemWithCommentsDto(
        Long id,
        String name, // — краткое название;
        String description, // — развёрнутое описание;
        Boolean available, // — статус о том, доступна или нет вещь для аренды;
        List<CommentDto> comments
) {}
