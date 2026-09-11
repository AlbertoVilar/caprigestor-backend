package com.devmaster.goatfarm.milk.persistence.mapper;

import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LactationPersistenceMapperTest {

    private final LactationPersistenceMapper mapper = new LactationPersistenceMapper();

    @Test
    void roundTripPreservesAllStateIncludingTechnicalGoatId() {
        LactationEntity entity = LactationEntity.builder()
                .id(4L).farmId(8L).goatId("RG-4").goatTechnicalId(44L)
                .status(LactationStatus.DRY)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 5, 1))
                .pregnancyStartDate(LocalDate.of(2024, 12, 1))
                .dryStartDate(LocalDate.of(2025, 5, 1))
                .dryAtPregnancyDays(90).restDays(60)
                .build();

        Lactation domain = mapper.toDomain(entity);
        LactationEntity roundTrip = mapper.toEntity(domain);

        assertEquals(entity.getId(), roundTrip.getId());
        assertEquals(entity.getFarmId(), roundTrip.getFarmId());
        assertEquals(entity.getGoatId(), roundTrip.getGoatId());
        assertEquals(entity.getGoatTechnicalId(), roundTrip.getGoatTechnicalId());
        assertEquals(entity.getStatus(), roundTrip.getStatus());
        assertEquals(entity.getStartDate(), roundTrip.getStartDate());
        assertEquals(entity.getEndDate(), roundTrip.getEndDate());
        assertEquals(entity.getPregnancyStartDate(), roundTrip.getPregnancyStartDate());
        assertEquals(entity.getDryStartDate(), roundTrip.getDryStartDate());
    }
}
