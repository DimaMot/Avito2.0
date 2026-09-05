package ru.project.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.project.booking.exceptions.ErrorResponse;
import ru.project.booking.exceptions.ForbiddenContentException;
import ru.project.booking.exceptions.NotFoundException;
import ru.project.booking.exceptions.ValidatedException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse NotFoundExceptionHandler(final NotFoundException e) {
        log.info("Ресурс не найден (404 Not Found): {}", e.getMessage());
        return new ErrorResponse("error", e.getMessage());
    }

    @ExceptionHandler(ValidatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse ValidateExceptionHandler(final ValidatedException e) {
        log.warn("Бизнес-валидация нарушена (400 Bad Request): {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse MethodArgumentNotValidExceptionHandler(final MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String field = error.getField();
                    String message = error.getDefaultMessage();
                    return "поле " + field + ": " + message;
                })
                .collect(Collectors.joining("; "));
        log.warn("Ошибка валидации JSON структуры (400 Bad Request): {}", errorMessage);
        return new ErrorResponse("Ошибка валидации входных данных", errorMessage);
    }

    @ExceptionHandler(ForbiddenContentException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse ForbiddenContentExceptionHandler(final ForbiddenContentException e) {
        log.warn("Попытка несанкционированного доступа (403 Forbidden): {}", e.getMessage());
        return new ErrorResponse("Доступ запрещен", e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnhandledException(final Throwable e) {
        log.error("Критическая системная ошибка сервера (500 Internal Server Error)", e);
        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
    }
}
