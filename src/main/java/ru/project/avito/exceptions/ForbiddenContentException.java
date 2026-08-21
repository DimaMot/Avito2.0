package ru.project.avito.exceptions;

public class ForbiddenContentException extends RuntimeException {
    public ForbiddenContentException(String message) {
        super(message);
    }
}
