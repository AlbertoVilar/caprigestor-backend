package com.devmaster.goatfarm.config.exceptions.custom;

public class InvalidArgumentException extends RuntimeException {

    private final String fieldName;

    public InvalidArgumentException(String msg) {
        super(msg);
        this.fieldName = null;
    }

    public InvalidArgumentException(String fieldName, String msg) {
        super(msg);
        this.fieldName = fieldName;
    }

    public InvalidArgumentException(String msg, Throwable cause) {
        super(msg, cause);
        this.fieldName = null;
    }

    public String getFieldName() {
        return fieldName;
    }
}