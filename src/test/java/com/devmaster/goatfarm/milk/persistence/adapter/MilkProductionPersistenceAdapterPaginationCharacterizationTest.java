package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import com.devmaster.goatfarm.milk.persistence.mapper.MilkProductionPersistenceMapper;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilkProductionPersistenceAdapterPaginationCharacterizationTest {

    @Mock
    private MilkProductionRepository repository;

    @Mock
    private GoatReferenceQueryPort goatReferenceQueryPort;

    private MilkProductionPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MilkProductionPersistenceAdapter(repository, goatReferenceQueryPort, new MilkProductionPersistenceMapper());
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId("BR123", 1L))
                .thenReturn(Optional.of(new GoatReference(new GoatId(7L), 1L, "BR123", "Cabra")));
    }

    @Test
    void technicalPageWithContentDoesNotInvokeLegacyFallback() {
        PageRequest pageable = PageRequest.of(1, 2);
        PageQuery query = new PageQuery(1, 2, List.of());
        MilkProductionEntity technicalEntity = entity(10L, 7L);
        when(repository.searchByTechnicalId(eq(1L), eq(7L), isNull(), isNull(), eq(pageable), eq(false)))
                .thenReturn(new PageImpl<>(List.of(technicalEntity), pageable, 3));

        PageResult<com.devmaster.goatfarm.milk.domain.MilkProduction> result =
                adapter.search(1L, "BR123", null, null, query, false);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(3);
        verify(repository, never()).search(anyLong(), anyString(), any(), any(), any(), anyBoolean());
    }

    @Test
    void emptyTechnicalPageInvokesLegacyFallbackAndPreservesLegacyMetadata() {
        PageRequest pageable = PageRequest.of(2, 5);
        PageQuery query = new PageQuery(2, 5, List.of());
        when(repository.searchByTechnicalId(eq(1L), eq(7L), isNull(), isNull(), eq(pageable), eq(true)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        MilkProductionEntity legacyEntity = entity(11L, null);
        when(repository.search(eq(1L), eq("BR123"), isNull(), isNull(), eq(pageable), eq(true)))
                .thenReturn(new PageImpl<>(List.of(legacyEntity), pageable, 6));

        PageResult<com.devmaster.goatfarm.milk.domain.MilkProduction> result =
                adapter.search(1L, "BR123", null, null, query, true);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(11);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(5);
        verify(repository).search(eq(1L), eq("BR123"), isNull(), isNull(), eq(pageable), eq(true));
    }

    @Test
    void emptyTechnicalAndLegacyPagesReturnEmptySelectedPage() {
        PageRequest pageable = PageRequest.of(3, 5);
        PageQuery query = new PageQuery(3, 5, List.of());
        when(repository.searchByTechnicalId(eq(1L), eq(7L), isNull(), isNull(), eq(pageable), eq(false)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(repository.search(eq(1L), eq("BR123"), isNull(), isNull(), eq(pageable), eq(false)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PageResult<com.devmaster.goatfarm.milk.domain.MilkProduction> result =
                adapter.search(1L, "BR123", null, null, query, false);

        assertThat(result.content()).isEmpty();
        assertThat(result.page()).isEqualTo(3);
        assertThat(result.size()).isEqualTo(5);
    }

    private MilkProductionEntity entity(Long id, Long technicalId) {
        return MilkProductionEntity.builder().id(id).farmId(1L).goatId("BR123").goatTechnicalId(technicalId)
                .date(LocalDate.of(2026, 2, 20)).shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.00")).status(MilkProductionStatus.ACTIVE)
                .recordedDuringMilkWithdrawal(false).build();
    }
}
