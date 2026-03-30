package com.devmaster.goatfarm.milk.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionAnnualSummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionDailySummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionMonthlySummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionUpsertRequestVO;

import java.time.LocalDate;

public interface FarmMilkProductionUseCase {

    FarmMilkProductionDailySummaryVO upsertDailyProduction(
            Long farmId,
            LocalDate productionDate,
            FarmMilkProductionUpsertRequestVO requestVO
    );

    FarmMilkProductionDailySummaryVO getDailySummary(Long farmId, LocalDate productionDate);

    FarmMilkProductionMonthlySummaryVO getMonthlySummary(Long farmId, int year, int month);

    FarmMilkProductionAnnualSummaryVO getAnnualSummary(Long farmId, int year);
}
