package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ValidationError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        String error = "Recurso não encontrado";
        HttpStatus status = HttpStatus.NOT_FOUND;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("resource", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ValidationError> database(DatabaseException e, HttpServletRequest request) {
        String error = "Erro de banco de dados";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("database", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ValidationError> invalidArgument(InvalidArgumentException e, HttpServletRequest request) {
        String error = "Argumento inválido";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("argument", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> methodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        String error = "Erro de validação de dados";
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            err.addError(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationError> validation(ValidationException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getValidationError());
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ValidationError> duplicate(DuplicateEntityException e, HttpServletRequest request) {
        String error = "Conflito de dados";
        HttpStatus status = HttpStatus.CONFLICT;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("duplicate", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ValidationError> unauthorized(UnauthorizedException e, HttpServletRequest request) {
        String error = "Não autorizado";
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("auth", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }
}
