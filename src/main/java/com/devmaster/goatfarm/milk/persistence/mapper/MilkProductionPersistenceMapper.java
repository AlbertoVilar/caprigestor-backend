package com.devmaster.goatfarm.milk.persistence.mapper;

import com.devmaster.goatfarm.milk.domain.MilkProduction;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import org.springframework.stereotype.Component;

/** Converts the framework-free milk model to and from its JPA representation. */
@Component
public class MilkProductionPersistenceMapper {
    public MilkProduction toDomain(MilkProductionEntity e) {
        if (e == null) return null;
        return MilkProduction.rehydrate(
                e.getId(), e.getFarmId(), e.getGoatId(), e.getGoatTechnicalId(),
                e.getLactation() == null ? null : e.getLactation().getId(),
                e.getDate(), e.getShift(), e.getVolumeLiters(), e.getNotes(), e.getStatus(),
                e.getCanceledAt(), e.getCanceledReason(), e.isRecordedDuringMilkWithdrawal(),
                e.getMilkWithdrawalEventId(), e.getMilkWithdrawalEndDate(), e.getMilkWithdrawalSource(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    public MilkProductionEntity toEntity(MilkProduction d) {
        if (d == null) return null;
        MilkProductionEntity e = new MilkProductionEntity();
        e.setId(d.getId()); e.setFarmId(d.getFarmId()); e.setGoatId(d.getGoatId());
        e.setGoatTechnicalId(d.getGoatTechnicalId());
        if (d.getLactationId() != null) {
            LactationEntity lactation = new LactationEntity();
            lactation.setId(d.getLactationId());
            e.setLactation(lactation);
        }
        e.setDate(d.getDate()); e.setShift(d.getShift()); e.setVolumeLiters(d.getVolumeLiters());
        e.setNotes(d.getNotes()); e.setStatus(d.getStatus()); e.setCanceledAt(d.getCanceledAt());
        e.setCanceledReason(d.getCanceledReason()); e.setRecordedDuringMilkWithdrawal(d.isRecordedDuringMilkWithdrawal());
        e.setMilkWithdrawalEventId(d.getMilkWithdrawalEventId()); e.setMilkWithdrawalEndDate(d.getMilkWithdrawalEndDate());
        e.setMilkWithdrawalSource(d.getMilkWithdrawalSource()); e.setCreatedAt(d.getCreatedAt()); e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}
