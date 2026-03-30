package com.devmaster.goatfarm.milk.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FarmMilkProductionDailySummaryDTO(
        LocalDate productionDate,
        boolean registered,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        String notes,
        LocalDateTime updatedAt
) {
}
