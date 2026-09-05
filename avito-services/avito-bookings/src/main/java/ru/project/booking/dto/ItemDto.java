package ru.project.booking.dto;

public record ItemDto(
        Long id,
        String name, // — краткое название;
        String description, // — развёрнутое описание;
        Boolean available, // — статус о том, доступна или нет вещь для аренды;
        Long ownerId
) {}
