package com.devmaster.goatfarm.config.exceptions.custom;

public class BusinessRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String fieldName;

    public BusinessRuleException(String msg) {
        super(msg);
        this.fieldName = null;
    }

    public BusinessRuleException(String fieldName, String msg) {
        super(msg);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
