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
        // Serialize enums by their name (e.g., ATIVO) to match test expectations
        return name();
    }
    
    @JsonCreator
    public static GoatStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        // Mapeamento de valores em inglês para português
        String normalizedValue = normalizeEnglishValues(value);
        
        for (GoatStatus status : GoatStatus.values()) {
            if (status.portugueseValue.equalsIgnoreCase(normalizedValue) || 
                status.name().equalsIgnoreCase(normalizedValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Valor inválido para GoatStatus: " + value);
    }
    
    /**
     * Normaliza valores em inglês para seus equivalentes em português/enum
     */
    private static String normalizeEnglishValues(String value) {
        if (value == null) return null;
        
        // Mapeamento de valores em inglês para valores aceitos pelo enum
        switch (value.toUpperCase()) {
            case "ACTIVE":
                return "ATIVO";
            case "INACTIVE":
                return "INACTIVE"; // Já está correto
            case "DECEASED":
                return "DECEASED"; // Já está correto
            case "SOLD":
                return "SOLD"; // Já está correto
            default:
                return value; // Retorna o valor original se não houver mapeamento
        }
    }
}
