package com.devmaster.goatfarm.commercial.business.bo;

import java.math.BigDecimal;

public record CommercialSummaryVO(
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
