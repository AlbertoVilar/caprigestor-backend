package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomError> ResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        CustomError err = new CustomError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    // Handles referential integrity violation errors
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<CustomError> handleDatabase(DatabaseException ex, WebRequest request) {
        CustomError err = new CustomError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<CustomError> DuplicateEntity(DuplicateEntityException ex, WebRequest request) {
        CustomError err = new CustomError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri = ", "")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    // Handles Bean Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomError> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        ValidationError error = new ValidationError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for one or more fields.",
                request.getDescription(false).replace("uri=", "")
        );
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

}
