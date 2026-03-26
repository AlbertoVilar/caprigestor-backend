package com.devmaster.goatfarm.commercial.api.dto;

import java.math.BigDecimal;

public record CommercialSummaryDTO(
        long customerCount,
        long animalSalesCount,
        BigDecimal animalSalesTotal,
        long milkSalesCount,
        BigDecimal milkSalesQuantityLiters,
        BigDecimal milkSalesTotal,
        long openReceivablesCount,
        BigDecimal openReceivablesTotal,
        long paidReceivablesCount,
        BigDecimal paidReceivablesTotal
) {
}
