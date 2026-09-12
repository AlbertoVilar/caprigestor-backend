package com.devmaster.goatfarm.reproduction.domain;

import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PregnancyDomainTest {

    @Test
    void confirmedPregnancyStartsActiveWithCreationState() {
        LocalDate breedingDate = LocalDate.of(2026, 1, 10);
        LocalDate confirmDate = LocalDate.of(2026, 1, 20);
        LocalDate dueDate = LocalDate.of(2026, 6, 8);

        Pregnancy pregnancy = Pregnancy.confirmed(7L, "RG-001", breedingDate, confirmDate, dueDate,
                "initial", 31L);

        assertEquals(PregnancyStatus.ACTIVE, pregnancy.getStatus());
        assertEquals(7L, pregnancy.getFarmId());
        assertEquals("RG-001", pregnancy.getGoatId());
        assertEquals(breedingDate, pregnancy.getBreedingDate());
        assertEquals(confirmDate, pregnancy.getConfirmDate());
        assertEquals(dueDate, pregnancy.getExpectedDueDate());
        assertEquals(31L, pregnancy.getCoverageEventId());
        assertNull(pregnancy.getClosedAt());
        assertNull(pregnancy.getCloseReason());
    }

    @Test
    void rehydrateRestoresPersistedStateAndMetadata() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 8, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 9, 45);

        Pregnancy pregnancy = Pregnancy.rehydrate(9L, 7L, "RG-009", 99L, PregnancyStatus.CLOSED,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 20), LocalDate.of(2026, 4, 30),
                LocalDate.of(2026, 1, 25), PregnancyCloseReason.FALSE_POSITIVE, "historical", 44L,
                createdAt, updatedAt);

        assertEquals(9L, pregnancy.getId());
        assertEquals(99L, pregnancy.getGoatTechnicalId());
        assertEquals(PregnancyStatus.CLOSED, pregnancy.getStatus());
        assertEquals(LocalDate.of(2025, 12, 1), pregnancy.getBreedingDate());
        assertEquals(LocalDate.of(2025, 12, 20), pregnancy.getConfirmDate());
        assertEquals(LocalDate.of(2026, 4, 30), pregnancy.getExpectedDueDate());
        assertEquals(LocalDate.of(2026, 1, 25), pregnancy.getClosedAt());
        assertEquals(PregnancyCloseReason.FALSE_POSITIVE, pregnancy.getCloseReason());
        assertEquals("historical", pregnancy.getNotes());
        assertEquals(44L, pregnancy.getCoverageEventId());
        assertEquals(createdAt, pregnancy.getCreatedAt());
        assertEquals(updatedAt, pregnancy.getUpdatedAt());
    }

    @Test
    void closeAndUpdateNotesChangeOnlyExpectedFields() {
        Pregnancy pregnancy = Pregnancy.confirmed(7L, "RG-001", LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 20), LocalDate.of(2026, 6, 8), "before", 31L);

        pregnancy.close(PregnancyCloseReason.BIRTH, LocalDate.of(2026, 5, 20));
        pregnancy.updateNotes("after");

        assertEquals(PregnancyStatus.CLOSED, pregnancy.getStatus());
        assertEquals(PregnancyCloseReason.BIRTH, pregnancy.getCloseReason());
        assertEquals(LocalDate.of(2026, 5, 20), pregnancy.getClosedAt());
        assertEquals("after", pregnancy.getNotes());
        assertEquals(7L, pregnancy.getFarmId());
        assertEquals("RG-001", pregnancy.getGoatId());
        assertEquals(31L, pregnancy.getCoverageEventId());
    }
}
