package com.devmaster.goatfarm.config.exceptions.custom;

public class ValidationException extends RuntimeException {

    private final ValidationError validationError;

    public ValidationException(ValidationError validationError) {
        super("Erro de validação");
        this.validationError = validationError;
    }

    public ValidationError getValidationError() {
        return validationError;
    }
}
