package com.devmaster.goatfarm.audit.enums;

public enum OperationalAuditActionType {

    GOAT_EXIT("Saida do rebanho"),
    ANIMAL_SALE_CREATED("Venda de animal"),
    ANIMAL_SALE_PAYMENT_REGISTERED("Recebimento de venda de animal"),
    MILK_SALE_CREATED("Venda de leite"),
    MILK_SALE_PAYMENT_REGISTERED("Recebimento de venda de leite");

    private final String label;

    OperationalAuditActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
