package com.devmaster.goatfarm.config.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webRequest.getDescription(false)).thenReturn("/api/test");
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        // Arrange
        String errorMessage = "Recurso não encontrado";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // Act
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleResourceNotFound(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Recurso não encontrado", response.getBody().getTitle());
        assertEquals(errorMessage, response.getBody().getDetail());
    }

    @Test
    void shouldHandleGenericException() {
        // Arrange
        String errorMessage = "Erro genérico";
        Exception exception = new Exception(errorMessage);

        // Act
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleAllOtherExceptions(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Erro interno do servidor", response.getBody().getTitle());
        assertEquals(errorMessage, response.getBody().getDetail());
    }

    @Test
    void shouldHandleDuplicateEntityException() {
        // Arrange
        String errorMessage = "Entidade duplicada";
        DuplicateEntityException exception = new DuplicateEntityException(errorMessage);

        // Act
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleDuplicateEntity(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Entidade duplicada", response.getBody().getTitle());
        assertEquals(errorMessage, response.getBody().getDetail());
    }

    @Test
    void shouldHandleValidationException() {
        // Arrange
        String errorMessage = "Erro de validação";
        Exception exception = new Exception(errorMessage);

        // Act
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleAllOtherExceptions(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Erro interno do servidor", response.getBody().getTitle());
        assertEquals(errorMessage, response.getBody().getDetail());
    }
}