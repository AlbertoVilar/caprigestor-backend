package com.devmaster.goatfarm.milk.persistence.mapper;

import com.devmaster.goatfarm.milk.domain.FarmMilkProduction;
import com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProductionEntity;
import org.springframework.stereotype.Component;

@Component
public class FarmMilkProductionPersistenceMapper {
    public FarmMilkProduction toDomain(FarmMilkProductionEntity e) {
        if (e == null) return null;
        return new FarmMilkProduction(e.getId(), e.getFarmId(), e.getProductionDate(), e.getTotalProduced(),
                e.getWithdrawalProduced(), e.getMarketableProduced(), e.getNotes(), e.getCreatedAt(), e.getUpdatedAt());
    }
    public FarmMilkProductionEntity toEntity(FarmMilkProduction d) {
        if (d == null) return null;
        return FarmMilkProductionEntity.builder().id(d.id()).farmId(d.farmId()).productionDate(d.productionDate())
                .totalProduced(d.totalProduced()).withdrawalProduced(d.withdrawalProduced())
                .marketableProduced(d.marketableProduced()).notes(d.notes()).createdAt(d.createdAt()).updatedAt(d.updatedAt()).build();
    }
}
