package com.devmaster.goatfarm.reproduction.persistence.mapper;

import com.devmaster.goatfarm.reproduction.domain.Pregnancy;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PregnancyPersistenceMapperTest {

    private final PregnancyPersistenceMapper mapper = new PregnancyPersistenceMapper();

    @Test
    void mapsPregnancyBothDirectionsWithoutDroppingFields() {
        LocalDate createdDate = LocalDate.of(2026, 1, 1);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 8, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 9, 45);
        Pregnancy source = Pregnancy.rehydrate(9L, 7L, "RG-009", 99L, PregnancyStatus.CLOSED,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 20), LocalDate.of(2026, 4, 30),
                createdDate, PregnancyCloseReason.FALSE_POSITIVE, "historical", 44L, createdAt, updatedAt);

        PregnancyEntity entity = mapper.toEntity(source);
        assertEquals(source.getId(), entity.getId());
        assertEquals(source.getFarmId(), entity.getFarmId());
        assertEquals(source.getGoatId(), entity.getGoatId());
        assertEquals(source.getGoatTechnicalId(), entity.getGoatTechnicalId());
        assertEquals(source.getStatus(), entity.getStatus());
        assertEquals(source.getBreedingDate(), entity.getBreedingDate());
        assertEquals(source.getConfirmDate(), entity.getConfirmDate());
        assertEquals(source.getExpectedDueDate(), entity.getExpectedDueDate());
        assertEquals(source.getClosedAt(), entity.getClosedAt());
        assertEquals(source.getCloseReason(), entity.getCloseReason());
        assertEquals(source.getNotes(), entity.getNotes());
        assertEquals(source.getCoverageEventId(), entity.getCoverageEventId());
        assertEquals(source.getCreatedAt(), entity.getCreatedAt());
        assertEquals(source.getUpdatedAt(), entity.getUpdatedAt());

        Pregnancy roundTrip = mapper.toDomain(entity);
        assertEquals(source.getId(), roundTrip.getId());
        assertEquals(source.getFarmId(), roundTrip.getFarmId());
        assertEquals(source.getGoatId(), roundTrip.getGoatId());
        assertEquals(source.getGoatTechnicalId(), roundTrip.getGoatTechnicalId());
        assertEquals(source.getStatus(), roundTrip.getStatus());
        assertEquals(source.getBreedingDate(), roundTrip.getBreedingDate());
        assertEquals(source.getConfirmDate(), roundTrip.getConfirmDate());
        assertEquals(source.getExpectedDueDate(), roundTrip.getExpectedDueDate());
        assertEquals(source.getClosedAt(), roundTrip.getClosedAt());
        assertEquals(source.getCloseReason(), roundTrip.getCloseReason());
        assertEquals(source.getNotes(), roundTrip.getNotes());
        assertEquals(source.getCoverageEventId(), roundTrip.getCoverageEventId());
        assertEquals(source.getCreatedAt(), roundTrip.getCreatedAt());
        assertEquals(source.getUpdatedAt(), roundTrip.getUpdatedAt());
    }
}
