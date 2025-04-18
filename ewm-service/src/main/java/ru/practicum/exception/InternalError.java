package ru.practicum.exception;

public class InternalError extends RuntimeException {
    public InternalError(String message) {
        super(message);
    }
}
