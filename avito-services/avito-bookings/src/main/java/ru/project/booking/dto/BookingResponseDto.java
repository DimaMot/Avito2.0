package ru.project.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.project.booking.model.BookingStatus;

import java.time.OffsetDateTime;

public record BookingResponseDto(
        long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Europe/Moscow")
        OffsetDateTime start,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Europe/Moscow")
        OffsetDateTime end,
        BookingStatus status,
        UserDto booker,
        ItemShortDto item
) {}

