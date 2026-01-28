package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void shouldHandleDataIntegrityViolationException_forDuplicateActivePregnancy() {
        Throwable rootCause = new RuntimeException("duplicate key value violates unique constraint \"ux_pregnancy_single_active_per_goat\"");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Constraint violation", rootCause);

        ResponseEntity<ValidationError> response = globalExceptionHandler.handleDataIntegrityViolation(exception, httpServletRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        ValidationError body = response.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), body.getStatus());
        assertEquals("Conflito de integridade de dados", body.getError());
        assertTrue(body.getErrors().stream().anyMatch(e ->
                "status".equals(e.getFieldName()) && "Já existe uma gestação ativa para esta cabra".equals(e.getMessage())));
    }

    @Test
    void shouldHandleDataIntegrityViolationException_genericCase() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Some other constraint violation");

        ResponseEntity<ValidationError> response = globalExceptionHandler.handleDataIntegrityViolation(exception, httpServletRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        ValidationError body = response.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), body.getStatus());
        assertEquals("Conflito de integridade de dados", body.getError());
        assertTrue(body.getErrors().stream().anyMatch(e ->
                "integrity".equals(e.getFieldName()) && "Violação de integridade no banco de dados".equals(e.getMessage())));
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        String errorMessage = "Recurso não encontrado";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        ResponseEntity<ValidationError> response = globalExceptionHandler.resourceNotFound(exception, httpServletRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        ValidationError body = response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), body.getStatus());
        assertEquals("Recurso não encontrado", body.getError());
        assertEquals("/api/test", body.getPath());
        assertTrue(body.getErrors().stream().anyMatch(e -> "resource".equals(e.getFieldName()) && errorMessage.equals(e.getMessage())));
    }

    @Test
    void shouldHandleInvalidArgumentException() {
        String errorMessage = "Erro genérico";
        InvalidArgumentException exception = new InvalidArgumentException(errorMessage);

        ResponseEntity<ValidationError> response = globalExceptionHandler.invalidArgument(exception, httpServletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        ValidationError body = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertEquals("Argumento inválido", body.getError());
        assertEquals("/api/test", body.getPath());
        assertTrue(body.getErrors().stream().anyMatch(e -> "argument".equals(e.getFieldName()) && errorMessage.equals(e.getMessage())));
    }

    @Test
    void shouldHandleDuplicateEntityException() {
        String errorMessage = "Entidade duplicada";
        DuplicateEntityException exception = new DuplicateEntityException(errorMessage);

        ResponseEntity<ValidationError> response = globalExceptionHandler.duplicate(exception, httpServletRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        ValidationError body = response.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), body.getStatus());
        assertEquals("Conflito de dados", body.getError());
        assertEquals("/api/test", body.getPath());
        assertTrue(body.getErrors().stream().anyMatch(e -> "duplicate".equals(e.getFieldName()) && errorMessage.equals(e.getMessage())));
    }

    @Test
    void shouldHandleValidationException() {
        // Create ValidationError with NULL path (simulating Business layer output)
        ValidationError validationError = new ValidationError(java.time.Instant.now(), 422, "Erro de validação", null);
        validationError.addError("field", "mensagem de erro");
        ValidationException exception = new ValidationException(validationError);

        ResponseEntity<ValidationError> response = globalExceptionHandler.validation(exception, httpServletRequest);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        ValidationError body = response.getBody();
        assertEquals(422, body.getStatus());
        assertEquals("Erro de validação", body.getError());
        assertEquals("/api/test", body.getPath()); // Verify path is populated from request
        assertTrue(body.getErrors().stream().anyMatch(e -> "field".equals(e.getFieldName()) && "mensagem de erro".equals(e.getMessage())));
    }
}
