package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.CustomError;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
