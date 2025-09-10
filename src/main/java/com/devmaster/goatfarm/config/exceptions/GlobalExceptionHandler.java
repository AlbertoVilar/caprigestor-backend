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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handles InvalidArgumentException
    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<CustomError> handleInvalidArgument(InvalidArgumentException ex, WebRequest request) {
        CustomError err = new CustomError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    // Handles ValidationException with custom validation errors
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationError> handleCustomValidation(ValidationException ex, WebRequest request) {
        ValidationError error = new ValidationError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        
        // Add custom validation errors
        ex.getValidationErrors().forEach(error::addError);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handles IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomError> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        CustomError err = new CustomError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    // Handles generic RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomError> handleRuntimeException(RuntimeException ex, WebRequest request) {
        CustomError err = new CustomError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    // Handles generic Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomError> handleGenericException(Exception ex, WebRequest request) {
        CustomError err = new CustomError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro inesperado. Tente novamente.",
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

}
