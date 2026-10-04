package com.devmaster.goatfarm.events.enums;

public enum EventType {
    COBERTURA,
    PARTO,
    MORTE,
    SAUDE,
    VACINACAO,
    TRANSFERENCIA,
    MUDANCA_PROPRIETARIO,
    PESAGEM,
    OUTRO;

    /**
     * Whether this value can be written through the generic Events module.
     * Legacy values remain in the enum so persisted history stays readable.
     */
    public boolean isGenericWritable() {
        return this == PESAGEM || this == OUTRO;
    }
}

