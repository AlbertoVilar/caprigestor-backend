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
    public String getPortugueseValue() {
        return portugueseValue;
    }
    
    @JsonCreator
    public static GoatStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (GoatStatus status : GoatStatus.values()) {
            if (status.portugueseValue.equalsIgnoreCase(value) || 
                status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Valor inválido para GoatStatus: " + value);
    }
}
