package com.devmaster.goatfarm.health.domain.enums;

public enum DoseUnit {
    ML("Mililitros"),
    MG("Miligramas"),
    G("Gramas"),
    UI("Unidades Internacionais"),
    TABLET("Comprimido"),
    FRASCO("Frasco"),
    DOSE("Dose"),
    OUTRO("Outro");

    private final String description;

    DoseUnit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
