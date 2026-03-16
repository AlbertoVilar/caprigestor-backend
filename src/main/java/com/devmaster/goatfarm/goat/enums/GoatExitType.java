package com.devmaster.goatfarm.goat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoatExitType {
    VENDA("Venda"),
    MORTE("Morte"),
    DESCARTE("Descarte"),
    DOACAO("Doacao"),
    TRANSFERENCIA("Transferencia");

    private final String portugueseValue;

    GoatExitType(String portugueseValue) {
        this.portugueseValue = portugueseValue;
    }

    @JsonValue
    public String getPortugueseValue() {
        return portugueseValue;
    }

    @JsonCreator
    public static GoatExitType fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (GoatExitType type : GoatExitType.values()) {
            if (type.name().equalsIgnoreCase(value) || type.portugueseValue.equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Valor inválido para GoatExitType: " + value);
    }
}
