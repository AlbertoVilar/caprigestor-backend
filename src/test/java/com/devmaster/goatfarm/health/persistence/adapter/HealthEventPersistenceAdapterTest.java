package com.devmaster.goatfarm.health.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.health.persistence.mapper.HealthEventPersistenceMapper;
import com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class HealthEventPersistenceAdapterTest {

    @Test
    void globalWithdrawalLookupUsesTechnicalGoatIdWithoutFarmPredicate() {
        HealthEventRepository repository = mock(HealthEventRepository.class);
        HealthEventPersistenceAdapter adapter = new HealthEventPersistenceAdapter(
                repository, mock(GoatReferenceQueryPort.class), mock(HealthEventPersistenceMapper.class));
        GoatId goatId = new GoatId(42L);

        when(repository.findPerformedWithWithdrawalByGoatTechnicalId(42L)).thenReturn(java.util.List.of());

        adapter.findPerformedWithWithdrawalByGoatTechnicalId(goatId);

        verify(repository).findPerformedWithWithdrawalByGoatTechnicalId(42L);
        verify(repository, never()).findPerformedWithWithdrawalByFarmIdAndGoatId(anyLong(), anyString());
        verify(repository, never()).findPerformedWithWithdrawalByFarmIdAndGoatTechnicalId(anyLong(), anyLong());
    }
}
