package ru.project.avito.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateDto(
        @NotNull(message = "не может быть пустым")
        String text) {
}
