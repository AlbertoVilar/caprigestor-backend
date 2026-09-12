package com.devmaster.goatfarm.milk.domain;

import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MilkProductionDomainTest {
    @Test
    void recordStartsActiveAndKeepsLactationId() {
        MilkProduction p = MilkProduction.record(1L, "RG-1", 9L, LocalDate.now().minusDays(1),
                MilkingShift.MORNING, new BigDecimal("2.50"), "note");
        assertEquals(MilkProductionStatus.ACTIVE, p.getStatus());
        assertEquals(9L, p.getLactationId());
        assertFalse(p.isRecordedDuringMilkWithdrawal());
    }

    @Test
    void updateAndCancelAreControlled() {
        MilkProduction p = MilkProduction.record(1L, "RG-1", 9L, LocalDate.now().minusDays(1),
                MilkingShift.MORNING, new BigDecimal("2.50"), null);
        p.updateDetails(new BigDecimal("3.00"), "updated");
        p.applyWithdrawalSnapshot(7L, LocalDate.now(), "medicine");
        p.cancel(LocalDateTime.now(), "correction");
        assertEquals(new BigDecimal("3.00"), p.getVolumeLiters());
        assertEquals(MilkProductionStatus.CANCELED, p.getStatus());
        assertTrue(p.isRecordedDuringMilkWithdrawal());
        assertThrows(IllegalStateException.class, () -> p.updateDetails(BigDecimal.ONE, null));
    }

    @Test
    void rehydratePreservesCompleteState() {
        LocalDateTime created = LocalDateTime.now().minusDays(2);
        MilkProduction p = MilkProduction.rehydrate(3L, 1L, "RG", 44L, 9L,
                LocalDate.now(), MilkingShift.AFTERNOON, BigDecimal.TEN, "n", MilkProductionStatus.CANCELED,
                created, "reason", true, 8L, LocalDate.now(), "source", created, created);
        assertEquals(44L, p.getGoatTechnicalId());
        assertEquals(9L, p.getLactationId());
        assertEquals(created, p.getCreatedAt());
        assertEquals(8L, p.getMilkWithdrawalEventId());
    }
}
