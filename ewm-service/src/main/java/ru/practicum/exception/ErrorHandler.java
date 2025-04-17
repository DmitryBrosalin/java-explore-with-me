package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        return Map.of(
                "status", "NOT_FOUND",
                "reason", "The required object was not found.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleInternalError(InternalError e) {
        return Map.of(
                "status", "INTERNAL_SERVER_ERROR",
                "reason", "Cannot read response from stat-server.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(ConflictException e) {
        return Map.of(
                "status", "FORBIDDEN",
                "reason", "For the requested operation the conditions are not met.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(BadRequestException e) {
        return Map.of(
                "status", "BAD_REQUEST",
                "reason", "Incorrectly made request.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().format(FORMATTER)
        );
    }
}
