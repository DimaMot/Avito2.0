package ru.project.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemCreatDto(
        @NotBlank(message = "не может быть пустым")
        String name, // — краткое название;
        @NotBlank(message = "не может быть пустым")
        String description, // — развёрнутое описание;
        @NotNull(message = "не может быть пустым")
        Boolean available // — статус о том, доступна или нет вещь для аренды;
) {
}
