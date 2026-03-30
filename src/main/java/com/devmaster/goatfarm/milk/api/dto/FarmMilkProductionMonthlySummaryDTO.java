package com.devmaster.goatfarm.milk.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record FarmMilkProductionMonthlySummaryDTO(
        int year,
        int month,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        int daysRegistered,
        List<FarmMilkProductionMonthlyDayItemDTO> dailyRecords
) {
}
