package ru.project.item.dto;

import jakarta.validation.constraints.NotNull;

public record CommentCreateDto(
        @NotNull(message = "не может быть пустым")
        String text
) {}
