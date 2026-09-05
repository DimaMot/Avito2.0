package ru.project.booking.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
        String error,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime errorTime
) {
    public ErrorResponse(String error, String description) {
        this(error, description, LocalDateTime.now());
    }
}
