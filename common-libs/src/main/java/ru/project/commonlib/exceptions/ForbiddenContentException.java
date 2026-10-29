package ru.project.commonlib.exceptions;

public class ForbiddenContentException extends RuntimeException {
    public ForbiddenContentException(String message) {
        super(message);
    }
}
