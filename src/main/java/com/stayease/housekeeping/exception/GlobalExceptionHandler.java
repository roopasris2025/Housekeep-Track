package com.stayease.housekeeping.exception;

import com.stayease.housekeeping.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/** One place that converts every exception into a clean {error, message} JSON. */
@RestControllerAdvice(basePackages = "com.stayease.housekeeping.controller")
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(RoomNotReadyException.class)
    public ResponseEntity<ErrorResponse> roomNotReady(RoomNotReadyException e) {
        return build(HttpStatus.CONFLICT, "ROOM_NOT_READY", e.getMessage());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> invalidTransition(InvalidStatusTransitionException e) {
        return build(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION", e.getMessage());
    }

    @ExceptionHandler(HousekeeperUnavailableException.class)
    public ResponseEntity<ErrorResponse> noHousekeeper(HousekeeperUnavailableException e) {
        return build(HttpStatus.CONFLICT, "HOUSEKEEPER_UNAVAILABLE", e.getMessage());
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> conflict(Exception e) {
        String msg = (e instanceof ConflictException) ? e.getMessage() : "Duplicate or linked data. Please check the values.";
        return build(HttpStatus.CONFLICT, "CONFLICT", msg);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> badLogin(InvalidCredentialsException e) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", e.getMessage());
    }

    // @Valid failed on a request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
    }

    @ExceptionHandler({IllegalArgumentException.class, PropertyReferenceException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception e) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Request body is missing or has an invalid value (check enum names and date format).");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> typeMismatch(MethodArgumentTypeMismatchException e) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid value for '" + e.getName() + "'.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> other(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong on the server.");
    }
}
