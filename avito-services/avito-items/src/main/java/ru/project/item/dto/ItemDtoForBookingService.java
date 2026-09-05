package ru.project.item.dto;

public record ItemDtoForBookingService(
        Long id,
        String name, // — краткое название;
        String description, // — развёрнутое описание;
        Boolean available, // — статус о том, доступна или нет вещь для аренды;
        Long ownerId
) {}
