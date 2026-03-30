package com.devmaster.goatfarm.milk.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record FarmMilkProductionAnnualSummaryDTO(
        int year,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        int daysRegistered,
        List<FarmMilkProductionAnnualMonthItemDTO> monthlyRecords
) {
}
