package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    PO("Puro de Origem"), // Purebred
    PA("Puro por Avaliação"), // Pure by Evaluation
    PC("Puro por Cruza"); // Crossbred
    
    private final String portugueseValue;
    
    Category(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }
    
    @JsonValue
    public String getPortugueseValue() {
        return portugueseValue;
    }
    
    @JsonCreator
    public static Category fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (Category category : Category.values()) {
            if (category.portugueseValue.equalsIgnoreCase(value) || 
                category.name().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Valor inválido para Category: " + value);
    }
}
