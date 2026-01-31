package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ValidationError> businessRule(BusinessRuleException e, HttpServletRequest request) {
        String error = "Regra de negócio violada";
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        String field = e.getFieldName() != null ? e.getFieldName() : "business_error";
        err.addError(field, e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ValidationError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        String error = "Recurso não encontrado";
        HttpStatus status = HttpStatus.NOT_FOUND;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("resource", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ValidationError> invalidArgument(InvalidArgumentException e, HttpServletRequest request) {
        String error = "Argumento inválido";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        String field = e.getFieldName() != null ? e.getFieldName() : "argument";
        err.addError(field, e.getMessage());
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationError> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        String error = "Argumento inválido";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("argument", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationError> handleAll(Exception e, HttpServletRequest request) {
        String error = "Erro interno do servidor";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("server", "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.");
        // Logar o erro original seria importante aqui, mas o handler já é chamado pelo Spring que loga
        return ResponseEntity.status(status).body(err);
    }



    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ValidationError> duplicate(DuplicateEntityException e, HttpServletRequest request) {
        String error = "Conflito de dados";
        HttpStatus status = HttpStatus.CONFLICT;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        String field = e.getFieldName() != null ? e.getFieldName() : "duplicate";
        err.addError(field, e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ValidationError> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        String error = "Conflito de integridade de dados";
        HttpStatus status = HttpStatus.CONFLICT;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        Throwable rootCause = e.getRootCause();
        String message = rootCause != null ? rootCause.getMessage() : e.getMessage();

        if (message != null && message.toLowerCase().contains("ux_pregnancy_single_active_per_goat")) {
            err.addError("status", "Já existe uma gestação ativa para esta cabra");
        } else {
            err.addError("integrity", "Violação de integridade no banco de dados");
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ValidationError> accessDenied(AccessDeniedException e, HttpServletRequest request) {
        String error = "Acesso negado";
        HttpStatus status = HttpStatus.FORBIDDEN;
        ValidationError err = new ValidationError(Instant.now(), status.value(), error, request.getRequestURI());
        err.addError("auth", e.getMessage());
        return ResponseEntity.status(status).body(err);
    }

}

