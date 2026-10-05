package ru.project.booking.exceptions;

public class ForbiddenContentException extends RuntimeException {
    public ForbiddenContentException(String message) {
        super(message);
    }
}
