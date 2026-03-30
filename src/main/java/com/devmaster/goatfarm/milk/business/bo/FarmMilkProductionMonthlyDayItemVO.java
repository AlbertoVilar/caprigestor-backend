package com.devmaster.goatfarm.milk.business.bo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FarmMilkProductionMonthlyDayItemVO(
        LocalDate productionDate,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        String notes
) {
}
