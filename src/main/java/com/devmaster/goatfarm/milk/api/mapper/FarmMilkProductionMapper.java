package com.devmaster.goatfarm.milk.api.mapper;

import com.devmaster.goatfarm.milk.api.dto.*;
import com.devmaster.goatfarm.milk.business.bo.*;
import org.springframework.stereotype.Component;

@Component
public class FarmMilkProductionMapper {

    public FarmMilkProductionUpsertRequestVO toRequestVO(FarmMilkProductionUpsertRequestDTO dto) {
        return new FarmMilkProductionUpsertRequestVO(
                dto.totalProduced(),
                dto.withdrawalProduced(),
                dto.marketableProduced(),
                dto.notes()
        );
    }

    public FarmMilkProductionDailySummaryDTO toDailySummaryDTO(FarmMilkProductionDailySummaryVO vo) {
        return new FarmMilkProductionDailySummaryDTO(
                vo.productionDate(),
                vo.registered(),
                vo.totalProduced(),
                vo.withdrawalProduced(),
                vo.marketableProduced(),
                vo.notes(),
                vo.updatedAt()
        );
    }

    public FarmMilkProductionMonthlySummaryDTO toMonthlySummaryDTO(FarmMilkProductionMonthlySummaryVO vo) {
        return new FarmMilkProductionMonthlySummaryDTO(
                vo.year(),
                vo.month(),
                vo.totalProduced(),
                vo.withdrawalProduced(),
                vo.marketableProduced(),
                vo.daysRegistered(),
                vo.dailyRecords().stream()
                        .map(item -> new FarmMilkProductionMonthlyDayItemDTO(
                                item.productionDate(),
                                item.totalProduced(),
                                item.withdrawalProduced(),
                                item.marketableProduced(),
                                item.notes()
                        ))
                        .toList()
        );
    }

    public FarmMilkProductionAnnualSummaryDTO toAnnualSummaryDTO(FarmMilkProductionAnnualSummaryVO vo) {
        return new FarmMilkProductionAnnualSummaryDTO(
                vo.year(),
                vo.totalProduced(),
                vo.withdrawalProduced(),
                vo.marketableProduced(),
                vo.daysRegistered(),
                vo.monthlyRecords().stream()
                        .map(item -> new FarmMilkProductionAnnualMonthItemDTO(
                                item.month(),
                                item.totalProduced(),
                                item.withdrawalProduced(),
                                item.marketableProduced(),
                                item.daysRegistered()
                        ))
                        .toList()
        );
    }
}
