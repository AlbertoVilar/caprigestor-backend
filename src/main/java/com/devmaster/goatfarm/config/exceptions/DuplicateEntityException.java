package com.devmaster.goatfarm.config.exceptions;

public class DuplicateEntityException extends RuntimeException {

    private final String fieldName;

    public DuplicateEntityException(String message) {
        super(message);
        this.fieldName = null;
    }

    public DuplicateEntityException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
