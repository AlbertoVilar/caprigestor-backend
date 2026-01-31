package com.devmaster.goatfarm.health.domain.enums;

public enum HealthEventType {
    VACINA("Vacina"),
    VERMIFUGACAO("Vermifugação"),
    MEDICACAO("Medicação"),
    PROCEDIMENTO("Procedimento"),
    DOENCA("Doença/Ocorrência");

    private final String description;

    HealthEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
