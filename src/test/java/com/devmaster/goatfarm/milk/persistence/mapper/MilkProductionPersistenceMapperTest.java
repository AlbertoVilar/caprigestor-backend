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
        LocalDate date = LocalDate.of(2026, 1, 10);
        LocalDate withdrawalEnd = LocalDate.of(2026, 1, 20);
        LocalDateTime canceledAt = LocalDateTime.of(2026, 1, 11, 8, 30);
        LocalDateTime created = LocalDateTime.of(2026, 1, 10, 7, 15);
        LocalDateTime updated = LocalDateTime.of(2026, 1, 11, 8, 30);
        MilkProduction d = MilkProduction.rehydrate(1L, 2L, "RG", 3L, 4L, date,
                MilkingShift.MORNING, new BigDecimal("2.10"), "n", MilkProductionStatus.CANCELED,
                canceledAt, "ajuste", true, 5L, withdrawalEnd, "source", created, updated);
        MilkProductionEntity e = mapper.toEntity(d);
        assertEquals(d.getId(), e.getId());
        assertEquals(d.getFarmId(), e.getFarmId());
        assertEquals(d.getGoatId(), e.getGoatId());
        assertEquals(d.getGoatTechnicalId(), e.getGoatTechnicalId());
        assertEquals(4L, e.getLactation().getId());
        assertEquals(d.getDate(), e.getDate());
        assertEquals(d.getShift(), e.getShift());
        assertEquals(d.getVolumeLiters(), e.getVolumeLiters());
        assertEquals(d.getNotes(), e.getNotes());
        assertEquals(d.getStatus(), e.getStatus());
        assertEquals(d.getCanceledAt(), e.getCanceledAt());
        assertEquals(d.getCanceledReason(), e.getCanceledReason());
        assertEquals(d.isRecordedDuringMilkWithdrawal(), e.isRecordedDuringMilkWithdrawal());
        assertEquals(d.getMilkWithdrawalEventId(), e.getMilkWithdrawalEventId());
        assertEquals(d.getMilkWithdrawalEndDate(), e.getMilkWithdrawalEndDate());
        assertEquals(d.getMilkWithdrawalSource(), e.getMilkWithdrawalSource());
        assertEquals(created, e.getCreatedAt());
        assertEquals(updated, e.getUpdatedAt());
        MilkProduction roundTrip = mapper.toDomain(e);
        assertEquals(d.getId(), roundTrip.getId());
        assertEquals(d.getFarmId(), roundTrip.getFarmId());
        assertEquals(d.getGoatId(), roundTrip.getGoatId());
        assertEquals(d.getLactationId(), roundTrip.getLactationId());
        assertEquals(d.getGoatTechnicalId(), roundTrip.getGoatTechnicalId());
        assertEquals(d.getDate(), roundTrip.getDate());
        assertEquals(d.getShift(), roundTrip.getShift());
        assertEquals(d.getVolumeLiters(), roundTrip.getVolumeLiters());
        assertEquals(d.getNotes(), roundTrip.getNotes());
        assertEquals(d.getStatus(), roundTrip.getStatus());
        assertEquals(d.getCanceledAt(), roundTrip.getCanceledAt());
        assertEquals(d.getCanceledReason(), roundTrip.getCanceledReason());
        assertEquals(d.isRecordedDuringMilkWithdrawal(), roundTrip.isRecordedDuringMilkWithdrawal());
        assertEquals(d.getMilkWithdrawalEventId(), roundTrip.getMilkWithdrawalEventId());
        assertEquals(d.getMilkWithdrawalEndDate(), roundTrip.getMilkWithdrawalEndDate());
        assertEquals(d.getMilkWithdrawalSource(), roundTrip.getMilkWithdrawalSource());
        assertEquals(d.getCreatedAt(), roundTrip.getCreatedAt());
        assertEquals(d.getUpdatedAt(), roundTrip.getUpdatedAt());
    }
}
