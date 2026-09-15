package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.milk.persistence.mapper.MilkProductionPersistenceMapper;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class MilkProductionPersistenceAdapterTest {

    @Test
    void globalDuplicateLookupUsesGoatTechnicalIdWithoutFarmContext() {
        MilkProductionRepository repository = mock(MilkProductionRepository.class);
        MilkProductionPersistenceAdapter adapter = new MilkProductionPersistenceAdapter(
                repository, mock(GoatReferenceQueryPort.class), new MilkProductionPersistenceMapper());
        GoatId goatId = new GoatId(42L);
        LocalDate date = LocalDate.of(2026, 9, 10);

        when(repository.existsActiveByGoatTechnicalIdAndDateAndShift(42L, date, MilkingShift.MORNING))
                .thenReturn(false);

        assertFalse(adapter.existsActiveByGoatTechnicalIdAndDateAndShift(goatId, date, MilkingShift.MORNING));
        verify(repository).existsActiveByGoatTechnicalIdAndDateAndShift(42L, date, MilkingShift.MORNING);
        verify(repository, never()).existsByFarmIdAndGoatIdAndDateAndShift(anyLong(), anyString(), any(), any());
        verify(repository, never()).existsByFarmIdAndGoatTechnicalIdAndDateAndShift(anyLong(), anyLong(), any(), any());
    }
}
