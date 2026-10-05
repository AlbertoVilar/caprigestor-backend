package com.devmaster.goatfarm.reproduction.persistence.mapper;

import com.devmaster.goatfarm.reproduction.domain.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEventEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReproductiveEventPersistenceMapperTest {

    private final ReproductiveEventPersistenceMapper mapper = new ReproductiveEventPersistenceMapper();

    @Test
    void mapsReproductiveEventBothDirectionsWithoutDroppingFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 8, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 9, 45);
        ReproductiveEvent source = ReproductiveEvent.rehydrate(11L, 7L, "RG-011", 111L,
                ReproductiveEventType.COVERAGE_CORRECTION, LocalDate.of(2026, 1, 15), BreedingType.AI,
                "BREEDER-7", "corrected", 22L, 33L, LocalDate.of(2026, 1, 14),
                LocalDate.of(2026, 3, 1), PregnancyCheckResult.PENDING, createdAt, updatedAt);

        ReproductiveEventEntity entity = mapper.toEntity(source);
        assertEquals(source.getId(), entity.getId());
        assertEquals(source.getFarmId(), entity.getFarmId());
        assertEquals(source.getGoatId(), entity.getGoatId());
        assertEquals(source.getGoatTechnicalId(), entity.getGoatTechnicalId());
        assertEquals(source.getEventType(), entity.getEventType());
        assertEquals(source.getEventDate(), entity.getEventDate());
        assertEquals(source.getBreedingType(), entity.getBreedingType());
        assertEquals(source.getBreederRef(), entity.getBreederRef());
        assertEquals(source.getNotes(), entity.getNotes());
        assertEquals(source.getPregnancyId(), entity.getPregnancyId());
        assertEquals(source.getRelatedEventId(), entity.getRelatedEventId());
        assertEquals(source.getCorrectedEventDate(), entity.getCorrectedEventDate());
        assertEquals(source.getCheckScheduledDate(), entity.getCheckScheduledDate());
        assertEquals(source.getCheckResult(), entity.getCheckResult());
        assertEquals(source.getCreatedAt(), entity.getCreatedAt());
        assertEquals(source.getUpdatedAt(), entity.getUpdatedAt());

        ReproductiveEvent roundTrip = mapper.toDomain(entity);
        assertEquals(source.getId(), roundTrip.getId());
        assertEquals(source.getFarmId(), roundTrip.getFarmId());
        assertEquals(source.getGoatId(), roundTrip.getGoatId());
        assertEquals(source.getGoatTechnicalId(), roundTrip.getGoatTechnicalId());
        assertEquals(source.getEventType(), roundTrip.getEventType());
        assertEquals(source.getEventDate(), roundTrip.getEventDate());
        assertEquals(source.getBreedingType(), roundTrip.getBreedingType());
        assertEquals(source.getBreederRef(), roundTrip.getBreederRef());
        assertEquals(source.getNotes(), roundTrip.getNotes());
        assertEquals(source.getPregnancyId(), roundTrip.getPregnancyId());
        assertEquals(source.getRelatedEventId(), roundTrip.getRelatedEventId());
        assertEquals(source.getCorrectedEventDate(), roundTrip.getCorrectedEventDate());
        assertEquals(source.getCheckScheduledDate(), roundTrip.getCheckScheduledDate());
        assertEquals(source.getCheckResult(), roundTrip.getCheckResult());
        assertEquals(source.getCreatedAt(), roundTrip.getCreatedAt());
        assertEquals(source.getUpdatedAt(), roundTrip.getUpdatedAt());
    }
}
