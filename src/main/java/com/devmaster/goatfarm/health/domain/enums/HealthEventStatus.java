package com.devmaster.goatfarm.health.domain.enums;

public enum HealthEventStatus {
    AGENDADO("Agendado"),
    REALIZADO("Realizado"),
    CANCELADO("Cancelado");

    private final String description;

    HealthEventStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
