package ru.project.avito.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.project.avito.booking.BookingStatus;
import ru.project.avito.item.dto.ItemDto;
import ru.project.avito.user.dto.UserDto;

import java.time.OffsetDateTime;

public record BookingResponseDto(
        long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Europe/Moscow")
        OffsetDateTime start,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Europe/Moscow")
        OffsetDateTime end,
        BookingStatus status,
        UserDto booker,
        ItemDto item
) {}
