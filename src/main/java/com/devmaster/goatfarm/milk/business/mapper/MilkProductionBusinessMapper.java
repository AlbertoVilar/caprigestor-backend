package com.devmaster.goatfarm.milk.business.mapper;

import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.domain.MilkProduction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MilkProductionBusinessMapper {
    public MilkProductionResponseVO toResponseVO(MilkProduction p) {
        if (p == null) return null;
        return MilkProductionResponseVO.builder().id(p.getId()).date(p.getDate()).shift(p.getShift())
                .volumeLiters(p.getVolumeLiters()).notes(p.getNotes()).status(p.getStatus())
                .recordedDuringMilkWithdrawal(p.isRecordedDuringMilkWithdrawal())
                .milkWithdrawalEventId(p.getMilkWithdrawalEventId()).milkWithdrawalEndDate(p.getMilkWithdrawalEndDate())
                .milkWithdrawalSource(p.getMilkWithdrawalSource()).canceledAt(p.getCanceledAt())
                .canceledReason(p.getCanceledReason()).build();
    }

    public List<MilkProductionResponseVO> toResponseVOList(List<MilkProduction> entities) {
        return entities == null ? List.of() : entities.stream().map(this::toResponseVO).toList();
    }
}
