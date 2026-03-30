package com.devmaster.goatfarm.milk.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FarmMilkProductionMonthlyDayItemDTO(
        LocalDate productionDate,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        String notes
) {
}
