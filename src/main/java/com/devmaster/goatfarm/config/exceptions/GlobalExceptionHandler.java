package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationError> httpMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        String error = "Erro de leitura do corpo da requisição";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());

        Throwable cause = e.getCause();
        if (cause instanceof UnrecognizedPropertyException upe) {
            err.addError(upe.getPropertyName(), "Campo não reconhecido no JSON");
        } else if (cause instanceof InvalidFormatException ife) {
            String field = (ife.getPath() != null && !ife.getPath().isEmpty() && ife.getPath().get(0).getFieldName() != null)
                    ? ife.getPath().get(0).getFieldName()
                    : "corpo";
            Object value = ife.getValue();
            String valueStr = value == null ? "null" : String.valueOf(value);
            err.addError(field, "Formato/valor inválido: '" + valueStr + "'");
        } else {
            String detail = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
            err.addError("json", detail);
        }

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
        ValidationError error = e.getValidationError();
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ValidationError> duplicate(DuplicateEntityException e, HttpServletRequest request) {
        String error = "Conflito de dados";
        HttpStatus status = HttpStatus.CONFLICT;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("duplicate", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ValidationError> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        String error = "Conflito de integridade de dados";
        HttpStatus status = HttpStatus.CONFLICT;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        Throwable rootCause = e.getRootCause();
        String message = rootCause != null ? rootCause.getMessage() : e.getMessage();

        if (message != null && message.contains("ux_pregnancy_single_active_per_goat")) {
            err.addError("status", "Duplicate active pregnancy for goat");
        } else {
            err.addError("integrity", "Database constraint violation");
        }
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

