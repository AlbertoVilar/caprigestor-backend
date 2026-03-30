package com.devmaster.goatfarm.milk.business.bo;

import java.math.BigDecimal;
import java.util.List;

public record FarmMilkProductionMonthlySummaryVO(
        int year,
        int month,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        int daysRegistered,
        List<FarmMilkProductionMonthlyDayItemVO> dailyRecords
) {
}
