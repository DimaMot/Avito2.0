package ru.project.commonlib.http.item;

public record ItemDtoForBookingService(
        Long id,
        String name, // — краткое название;
        String description, // — развёрнутое описание;
        Boolean available, // — статус о том, доступна или нет вещь для аренды;
        Long ownerId
) {}
