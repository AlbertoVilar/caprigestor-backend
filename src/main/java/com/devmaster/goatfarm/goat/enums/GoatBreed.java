package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoatBreed {
    ALPINE("Alpina"),
    ANGLO_NUBIANA("Anglo Nubiana"),
    BOER("Boer"),
    MESTIÇA("Mestiça"),
    MURCIANA_GRANADINA("Murciana Granadina"),
    ALPINA("Alpina Francesa"),
    SAANEN("Saanen"),
    TOGGENBURG("Toggenburg");
    
    private final String portugueseValue;
    
    GoatBreed(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }
    
    @JsonValue
    public String getPortugueseValue() {
        return portugueseValue;
    }
    
    @JsonCreator
    public static GoatBreed fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (GoatBreed breed : GoatBreed.values()) {
            if (breed.portugueseValue.equalsIgnoreCase(value) || 
                breed.name().equalsIgnoreCase(value)) {
                return breed;
            }
        }
        throw new IllegalArgumentException("Valor inválido para GoatBreed: " + value);
    }
}
