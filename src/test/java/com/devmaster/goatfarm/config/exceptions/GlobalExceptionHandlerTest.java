package com.devmaster.goatfarm.config.exceptions;

import com.devmaster.goatfarm.config.exceptions.custom.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        ResponseEntity<CustomError> response = globalExceptionHandler.ResourceNotFound(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals(errorMessage, response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleUnauthorizedException() {
        // Arrange
        String errorMessage = "Acesso não autorizado";
        UnauthorizedException exception = new UnauthorizedException(errorMessage);

        // Act
        ResponseEntity<CustomError> response = globalExceptionHandler.handleUnauthorized(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals(errorMessage, response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleForbiddenException() {
        // Arrange
        String errorMessage = "Acesso proibido";
        ForbiddenException exception = new ForbiddenException(errorMessage);

        // Act
        ResponseEntity<CustomError> response = globalExceptionHandler.handleForbidden(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatus());
        assertEquals(errorMessage, response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleDatabaseException() {
        // Arrange
        String errorMessage = "Erro de integridade referencial";
        DatabaseException exception = new DatabaseException(errorMessage);

        // Act
        ResponseEntity<CustomError> response = globalExceptionHandler.handleDatabase(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals(errorMessage, response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleDuplicateEntityException() {
        // Arrange
        String errorMessage = "Entidade duplicada";
        DuplicateEntityException exception = new DuplicateEntityException(errorMessage);

        // Act
        ResponseEntity<CustomError> response = globalExceptionHandler.DuplicateEntity(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals(errorMessage, response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleInvalidArgumentException() {
        // Arrange
        String errorMessage = "Argumento inválido";
        InvalidArgumentException exception = new InvalidArgumentException(errorMessage);

        // Act
        ResponseEntity<CustomError> response = globalExceptionHandler.handleInvalidArgument(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals(errorMessage, response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        // Arrange
        String errorMessage = "Dados inválidos";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // Act
        ResponseEntity<CustomError> response = globalExceptionHandler.handleIllegalArgument(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Dados inválidos: " + errorMessage, response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleGenericRuntimeException() {
        // Arrange
        String errorMessage = "Erro de runtime";
        RuntimeException exception = new RuntimeException(errorMessage);

        // Act
        ResponseEntity<CustomError> response = globalExceptionHandler.handleRuntimeException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Erro interno do servidor: " + errorMessage, response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void shouldHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Erro genérico");

        // Act
        ResponseEntity<CustomError> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Erro inesperado. Tente novamente.", response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }
}