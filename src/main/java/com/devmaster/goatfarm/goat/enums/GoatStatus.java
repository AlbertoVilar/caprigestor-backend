package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoatStatus {
    ATIVO("Ativo"),
    INACTIVE("Inativo"),
    DECEASED("Falecido"),
    SOLD("Vendido");
    
    private final String portugueseValue;
    
    GoatStatus(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }
    
    @JsonValue
    public String getValue() {
                return name();
    }
    
    @JsonCreator
    public static GoatStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        
                String normalizedValue = normalizeEnglishValues(value);
        
        for (GoatStatus status : GoatStatus.values()) {
            if (status.portugueseValue.equalsIgnoreCase(normalizedValue) || 
                status.name().equalsIgnoreCase(normalizedValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Valor invÃ¡lido para GoatStatus: " + value);
    }
    
    /**
     * Normaliza valores em inglÃªs para seus equivalentes em portuguÃªs/enum
     */
    private static String normalizeEnglishValues(String value) {
        if (value == null) return null;
        
                switch (value.toUpperCase()) {
            case "ACTIVE":
                return "ATIVO";
            case "INACTIVE":
                return "INACTIVE";             case "DECEASED":
                return "DECEASED";             case "SOLD":
                return "SOLD";             default:
                return value;         }
    }
}

