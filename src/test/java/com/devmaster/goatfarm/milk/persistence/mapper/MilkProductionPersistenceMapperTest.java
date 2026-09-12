package com.devmaster.goatfarm.milk.persistence.mapper;

import com.devmaster.goatfarm.milk.domain.MilkProduction;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MilkProductionPersistenceMapperTest {
    private final MilkProductionPersistenceMapper mapper = new MilkProductionPersistenceMapper();

    @Test
    void mapsBothDirectionsIncludingRelationAndTimestamps() {
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        MilkProduction d = MilkProduction.rehydrate(1L, 2L, "RG", 3L, 4L, LocalDate.now(),
                MilkingShift.MORNING, new BigDecimal("2.10"), "n", MilkProductionStatus.ACTIVE,
                null, null, true, 5L, LocalDate.now(), "source", created, created);
        MilkProductionEntity e = mapper.toEntity(d);
        assertEquals(4L, e.getLactation().getId());
        assertEquals(created, e.getCreatedAt());
        MilkProduction roundTrip = mapper.toDomain(e);
        assertEquals(d.getLactationId(), roundTrip.getLactationId());
        assertEquals(d.getGoatTechnicalId(), roundTrip.getGoatTechnicalId());
        assertEquals(d.getMilkWithdrawalEventId(), roundTrip.getMilkWithdrawalEventId());
        assertEquals(created, roundTrip.getUpdatedAt());
    }
}
