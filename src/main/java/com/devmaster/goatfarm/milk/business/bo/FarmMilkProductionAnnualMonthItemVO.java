package com.devmaster.goatfarm.milk.business.bo;

import java.math.BigDecimal;

public record FarmMilkProductionAnnualMonthItemVO(
        int month,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        int daysRegistered
) {
}
