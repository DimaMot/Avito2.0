package ru.project.avito.item.dto;

import ru.project.avito.booking.dto.BookingShortDto;

import java.util.List;

public record ItemWithBookingDto(
        Long id,
        String name, // — краткое название;
        String description, // — развёрнутое описание;
        Boolean available, // — статус о том, доступна или нет вещь для аренды;
        List<CommentDto> comments,
        BookingShortDto prevBooking,
        BookingShortDto nextBooking
){}
