package com.devmaster.goatfarm.commercial.api.dto;

import java.math.BigDecimal;

public record MonthlyOperationalSummaryDTO(
        int year,
        int month,
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal balance,
        BigDecimal animalSalesRevenue,
        BigDecimal milkSalesRevenue,
        BigDecimal operationalExpensesTotal,
        BigDecimal inventoryPurchaseCostsTotal
) {
}
