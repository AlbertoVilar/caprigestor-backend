package com.devmaster.goatfarm.milk.business.bo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FarmMilkProductionDailySummaryVO(
        LocalDate productionDate,
        boolean registered,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        String notes,
        LocalDateTime updatedAt
) {
}
