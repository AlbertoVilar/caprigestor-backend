package com.devmaster.goatfarm.milk.business.bo;

import java.math.BigDecimal;
import java.util.List;

public record FarmMilkProductionAnnualSummaryVO(
        int year,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        int daysRegistered,
        List<FarmMilkProductionAnnualMonthItemVO> monthlyRecords
) {
}
