package ru.project.avito.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record BookingCreateDto(
        @NotNull(message = "ID вещи не может быть пустым")
        long itemId,

        @NotNull(message = "Начало бронирования вещи не может быть пустым")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @FutureOrPresent(message = "Бронирование не должно начинаться в прошлом")
        OffsetDateTime start, //  — дата и время начала бронирования;

        @NotNull(message = "Конец бронирования вещи не может быть пустым")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @Future(message = "Бронирование должно заканчиваться в будущем")
        OffsetDateTime end // — дата и время конца бронирования;
) {}
