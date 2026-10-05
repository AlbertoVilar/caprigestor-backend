package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.mapper.LactationPersistenceMapper;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LactationPersistenceAdapterTest {

    @Mock
    private LactationRepository repository;

    @Mock
    private GoatReferenceQueryPort goatReferenceQueryPort;

    private LactationPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LactationPersistenceAdapter(repository, goatReferenceQueryPort, new LactationPersistenceMapper());
    }

    @Test
    void findAllMapsNeutralPageQueryAndPreservesTechnicalFirstFallback() {
        PageQuery query = new PageQuery(1, 2, List.of());
        PageRequest pageable = PageRequest.of(1, 2);
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId("RG-1", 1L))
                .thenReturn(Optional.of(new GoatReference(new GoatId(7L), 1L, "RG-1", "Cabra")));
        when(repository.findAllByFarmIdAndGoatTechnicalId(1L, 7L, pageable))
                .thenReturn(new PageImpl<>(List.of(entity(10L, 7L)), pageable, 3));

        PageResult<com.devmaster.goatfarm.milk.domain.Lactation> result = adapter.findAllByFarmIdAndGoatId(1L, "RG-1", query);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.page()).isEqualTo(1);
        verify(repository, never()).findAllByFarmIdAndGoatId(eq(1L), eq("RG-1"), eq(pageable));
    }

    @Test
    void findLatestUsesTechnicalIdentityAndKeepsStartDateThenIdOrderingInRepositoryContract() {
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId("RG-1", 1L))
                .thenReturn(Optional.of(new GoatReference(new GoatId(7L), 1L, "RG-1", "Cabra")));
        LactationEntity latest = entity(12L, 7L);
        when(repository.findFirstByFarmIdAndGoatTechnicalIdOrderByStartDateDescIdDesc(1L, 7L))
                .thenReturn(Optional.of(latest));

        Optional<com.devmaster.goatfarm.milk.domain.Lactation> result = adapter.findLatestByFarmIdAndGoatId(1L, "RG-1");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(12L);
        verify(repository, never()).findFirstByFarmIdAndGoatIdOrderByStartDateDescIdDesc(1L, "RG-1");
    }

    @Test
    void findByIdAndTechnicalIdUsesNeutralGlobalLookupWithoutFarmFilter() {
        LactationEntity inherited = entity(12L, 7L);
        when(repository.findByIdAndGoatTechnicalId(12L, 7L)).thenReturn(Optional.of(inherited));

        Optional<com.devmaster.goatfarm.milk.domain.Lactation> result =
                adapter.findByIdAndGoatTechnicalId(12L, new GoatId(7L));

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getFarmId()).isEqualTo(1L);
        verify(repository).findByIdAndGoatTechnicalId(12L, 7L);
        verify(repository, never()).findByIdAndFarmIdAndGoatId(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    private LactationEntity entity(Long id, Long technicalId) {
        return LactationEntity.builder().id(id).farmId(1L).goatId("RG-1").goatTechnicalId(technicalId)
                .status(LactationStatus.DRY).startDate(LocalDate.of(2026, 1, 1)).build();
    }
}
