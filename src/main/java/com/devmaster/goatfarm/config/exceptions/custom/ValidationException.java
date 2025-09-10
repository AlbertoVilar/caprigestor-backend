package com.devmaster.goatfarm.config.exceptions.custom;

import java.util.Map;

public class ValidationException extends RuntimeException {

    private final Map<String, String> validationErrors;

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}