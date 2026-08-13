package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCreatDto {
    @NotBlank(message = "не может быть пустым")
    private String name; // — краткое название;
    @NotBlank(message = "не может быть пустым")
    private String description; // — развёрнутое описание;
    @NotNull(message = "не может быть пустым")
    private Boolean available; // — статус о том, доступна или нет вещь для аренды;
}
