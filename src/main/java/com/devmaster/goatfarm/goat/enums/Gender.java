package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("Macho"),
    FEMALE("Fêmea");
    
    private final String portugueseValue;
    
    Gender(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }
    
    @JsonValue
    public String getPortugueseValue() {
        return portugueseValue;
    }
    
    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (Gender gender : Gender.values()) {
            if (gender.portugueseValue.equalsIgnoreCase(value) || 
                gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        
        // Aceitar valores específicos em português que podem estar no banco
        if ("MACHO".equalsIgnoreCase(value)) {
            return MALE;
        }
        if ("FÊMEA".equalsIgnoreCase(value) || "FEMEA".equalsIgnoreCase(value)) {
            return FEMALE;
        }
        
        // Aceitar valores em inglês que podem estar no banco de dados
        if ("MALE".equalsIgnoreCase(value)) {
            return MALE;
        }
        if ("FEMALE".equalsIgnoreCase(value)) {
            return FEMALE;
        }
        
        throw new IllegalArgumentException("Valor inválido para Gender: " + value);
    }
}
