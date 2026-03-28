package com.devmaster.goatfarm.commercial.business.bo;

import java.math.BigDecimal;

public record MonthlyOperationalSummaryVO(
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
