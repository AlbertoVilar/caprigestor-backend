package com.devmaster.goatfarm.milk.api.dto;

import java.math.BigDecimal;

public record FarmMilkProductionAnnualMonthItemDTO(
        int month,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        int daysRegistered
) {
}
