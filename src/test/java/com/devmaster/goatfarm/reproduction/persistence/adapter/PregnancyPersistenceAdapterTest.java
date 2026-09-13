package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PregnancyPersistenceAdapterTest {

    @Mock
    private PregnancyRepository repository;

    @Mock
    private GoatReferenceQueryPort goatReferenceQueryPort;

    @Test
    void history_shouldFallbackToLegacyWhenTechnicalPageIsEmpty() {
        Long farmId = 7L;
        String registration = "RG-007";
        PageRequest page = PageRequest.of(1, 2);
        PregnancyEntity legacy = PregnancyEntity.builder().id(42L).farmId(farmId)
                .goatId(registration).status(PregnancyStatus.CLOSED).build();
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registration, farmId))
                .thenReturn(Optional.of(new GoatReference(new GoatId(99L), farmId, registration, "Doe")));
        when(repository.findAllByFarmIdAndGoatTechnicalIdOrderByBreedingDateDescIdDesc(farmId, 99L, page))
                .thenReturn(new PageImpl<>(List.of(), page, 0));
        when(repository.findAllByFarmIdAndGoatIdOrderByBreedingDateDescIdDesc(farmId, registration, page))
                .thenReturn(new PageImpl<>(List.of(legacy), page, 3));

        var result = new PregnancyPersistenceAdapter(repository, goatReferenceQueryPort)
                .findAllByFarmIdAndGoatId(farmId, registration, page);

        assertThat(result.getContent()).extracting("id").containsExactly(42L);
        assertThat(result.getTotalElements()).isEqualTo(3);
        verify(repository).findAllByFarmIdAndGoatIdOrderByBreedingDateDescIdDesc(farmId, registration, page);
    }
}
