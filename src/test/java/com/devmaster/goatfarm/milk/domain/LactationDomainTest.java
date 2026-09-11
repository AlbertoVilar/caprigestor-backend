package com.devmaster.goatfarm.milk.domain;

import com.devmaster.goatfarm.milk.enums.LactationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LactationDomainTest {

    @Test
    void openCreatesActiveLactationWithDefaults() {
        Lactation lactation = Lactation.open(7L, "RG-1", LocalDate.of(2026, 1, 10));

        assertNull(lactation.getId());
        assertEquals(7L, lactation.getFarmId());
        assertEquals("RG-1", lactation.getGoatId());
        assertEquals(LactationStatus.ACTIVE, lactation.getStatus());
        assertEquals(90, lactation.getDryAtPregnancyDays());
        assertEquals(60, lactation.getRestDays());
        assertNull(lactation.getEndDate());
    }

    @Test
    void dryMovesActiveLactationToDryAndSetsDates() {
        Lactation lactation = Lactation.open(7L, "RG-1", LocalDate.of(2026, 1, 10));

        lactation.dry(LocalDate.of(2026, 4, 1));

        assertEquals(LactationStatus.DRY, lactation.getStatus());
        assertEquals(LocalDate.of(2026, 4, 1), lactation.getEndDate());
        assertEquals(LocalDate.of(2026, 4, 1), lactation.getDryStartDate());
    }

    @Test
    void resumeMovesDryLactationBackToActive() {
        Lactation lactation = Lactation.open(7L, "RG-1", LocalDate.of(2026, 1, 10));
        lactation.dry(LocalDate.of(2026, 4, 1));

        lactation.resume();

        assertEquals(LactationStatus.ACTIVE, lactation.getStatus());
        assertNull(lactation.getEndDate());
        assertNull(lactation.getDryStartDate());
    }

    @Test
    void rejectsInvalidOpenAndDryTransitions() {
        assertThrows(IllegalArgumentException.class,
                () -> Lactation.open(null, "RG-1", LocalDate.now()));
        Lactation active = Lactation.open(7L, "RG-1", LocalDate.of(2026, 1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> active.dry(LocalDate.of(2026, 1, 9)));
        assertThrows(IllegalArgumentException.class, () -> active.dry(null));
        assertThrows(IllegalStateException.class, active::resume);
    }

    @Test
    void rejectsDryingAlreadyDryLactation() {
        Lactation lactation = Lactation.open(7L, "RG-1", LocalDate.of(2026, 1, 10));
        lactation.dry(LocalDate.of(2026, 4, 1));

        assertThrows(IllegalStateException.class,
                () -> lactation.dry(LocalDate.of(2026, 4, 2)));
    }

    @Test
    void rehydratePreservesTechnicalIdentityAndHistory() {
        LocalDate start = LocalDate.of(2025, 3, 2);
        LocalDate end = LocalDate.of(2025, 7, 2);
        Lactation lactation = Lactation.rehydrate(9L, 7L, "RG-1", 99L,
                LactationStatus.DRY, start, end, LocalDate.of(2025, 1, 1), end,
                80, 55, null, null);

        assertEquals(9L, lactation.getId());
        assertEquals(99L, lactation.getGoatTechnicalId());
        assertEquals(LactationStatus.DRY, lactation.getStatus());
        assertEquals(end, lactation.getEndDate());
        assertEquals(80, lactation.getDryAtPregnancyDays());
    }
}
