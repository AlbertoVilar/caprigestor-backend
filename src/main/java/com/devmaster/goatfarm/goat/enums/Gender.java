package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("Macho"),
    FEMALE("FÃªmea");
    
    private final String portugueseValue;
    
    Gender(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }
    
    @JsonValue
    public String getValue() {
                return name();
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
        
                if ("MACHO".equalsIgnoreCase(value)) {
            return MALE;
        }
        if ("FÃŠMEA".equalsIgnoreCase(value) || "FEMEA".equalsIgnoreCase(value)) {
            return FEMALE;
        }
        
                if ("MALE".equalsIgnoreCase(value)) {
            return MALE;
        }
        if ("FEMALE".equalsIgnoreCase(value)) {
            return FEMALE;
        }
        
        throw new IllegalArgumentException("Valor invÃ¡lido para Gender: " + value);
    }
}

