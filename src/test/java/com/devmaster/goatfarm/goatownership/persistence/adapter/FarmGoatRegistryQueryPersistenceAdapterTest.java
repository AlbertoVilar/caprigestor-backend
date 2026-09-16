package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatRegistryQueryPort;
import com.devmaster.goatfarm.goatownership.domain.CreatorSource;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.persistence.entity.CreatorReferenceEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import com.devmaster.goatfarm.goatownership.persistence.repository.CreatorReferenceRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmGoatRegistryQueryPersistenceAdapterTest {

    private static final Instant T = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private CreatorReferenceRepository creators;

    @Mock
    private GoatOwnershipPeriodRepository periods;

    @Mock
    private GoatRepository goats;

    @Spy
    private OwnershipPersistenceMapper mapper = new OwnershipPersistenceMapper();

    @InjectMocks
    private FarmGoatRegistryQueryPersistenceAdapter adapter;

    @Test
    @DisplayName("Returns empty list immediately when farm has no created goats and no local ownership periods")
    void findCandidates_emptyWhenNoCreationsAndNoOwnership() {
        when(creators.findByCreatorFarmId(1L)).thenReturn(List.of());
        when(periods.findByFarmIdOrderByGoatIdAscStartedAtAscIdAsc(1L)).thenReturn(List.of());

        List<FarmGoatRegistryQueryPort.Candidate> candidates = adapter.findCandidates(1L);

        assertThat(candidates).isEmpty();
        verify(creators).findByCreatorFarmId(1L);
        verify(periods).findByFarmIdOrderByGoatIdAscStartedAtAscIdAsc(1L);
        verifyNoMoreInteractions(creators, periods, goats);
    }

    @Test
    @DisplayName("Finds candidates by creator reference and populates ownership history")
    void findCandidates_createdByFarm() {
        CreatorReferenceEntity creatorEntity = new CreatorReferenceEntity();
        creatorEntity.setGoatId(10L);
        creatorEntity.setCreatorTod("TODA");
        creatorEntity.setCreatorFarmId(1L);
        creatorEntity.setCreatorNameSnapshot("Capril A");
        creatorEntity.setSource(CreatorSource.BIRTH);
        creatorEntity.setRecordedAt(T);

        GoatEntity goatEntity = new GoatEntity();
        goatEntity.setTechnicalId(10L);
        goatEntity.setRegistrationNumber("RG10");
        goatEntity.setName("Goat 10");
        goatEntity.setStatus(GoatStatus.ATIVO);

        when(creators.findByCreatorFarmId(1L)).thenReturn(List.of(creatorEntity));
        when(periods.findByFarmIdOrderByGoatIdAscStartedAtAscIdAsc(1L)).thenReturn(List.of());
        when(creators.findAllById(Set.of(10L))).thenReturn(List.of(creatorEntity));
        when(periods.findByGoatIdInOrderByGoatIdAscStartedAtAscIdAsc(List.of(10L))).thenReturn(List.of());
        when(goats.findAllById(Set.of(10L))).thenReturn(List.of(goatEntity));

        List<FarmGoatRegistryQueryPort.Candidate> candidates = adapter.findCandidates(1L);

        assertThat(candidates).hasSize(1);
        FarmGoatRegistryQueryPort.Candidate candidate = candidates.getFirst();
        assertThat(candidate.goatId()).isEqualTo(new GoatId(10L));
        assertThat(candidate.registrationNumber()).isEqualTo("RG10");
        assertThat(candidate.name()).isEqualTo("Goat 10");
        assertThat(candidate.globalStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(candidate.creatorReference()).isNotNull();
        assertThat(candidate.creatorReference().creatorFarmId()).isEqualTo(1L);
        assertThat(candidate.ownershipHistory()).isEmpty();
    }

    @Test
    @DisplayName("Preserves external creator reference when goat is owned by queried farm")
    void findCandidates_acquiredGoatPreservesExternalCreatorProvenance() {
        GoatOwnershipPeriodEntity periodEntity = new GoatOwnershipPeriodEntity();
        periodEntity.setId(100L);
        periodEntity.setGoatId(20L);
        periodEntity.setFarmId(1L);
        periodEntity.setStartedAt(T);
        periodEntity.setEntryType(OwnershipEntryType.TRANSFER_IN);
        periodEntity.setSource("test");

        CreatorReferenceEntity externalCreator = new CreatorReferenceEntity();
        externalCreator.setGoatId(20L);
        externalCreator.setCreatorTod("TODB");
        externalCreator.setCreatorFarmId(2L);
        externalCreator.setCreatorNameSnapshot("Capril B");
        externalCreator.setSource(CreatorSource.ABCC);
        externalCreator.setRecordedAt(T);

        GoatEntity goatEntity = new GoatEntity();
        goatEntity.setTechnicalId(20L);
        goatEntity.setRegistrationNumber("RG20");
        goatEntity.setName("Goat 20");
        goatEntity.setStatus(GoatStatus.ATIVO);

        when(creators.findByCreatorFarmId(1L)).thenReturn(List.of());
        when(periods.findByFarmIdOrderByGoatIdAscStartedAtAscIdAsc(1L)).thenReturn(List.of(periodEntity));
        when(creators.findAllById(Set.of(20L))).thenReturn(List.of(externalCreator));
        when(periods.findByGoatIdInOrderByGoatIdAscStartedAtAscIdAsc(List.of(20L))).thenReturn(List.of(periodEntity));
        when(goats.findAllById(Set.of(20L))).thenReturn(List.of(goatEntity));

        List<FarmGoatRegistryQueryPort.Candidate> candidates = adapter.findCandidates(1L);

        assertThat(candidates).hasSize(1);
        FarmGoatRegistryQueryPort.Candidate candidate = candidates.getFirst();
        assertThat(candidate.goatId()).isEqualTo(new GoatId(20L));
        assertThat(candidate.creatorReference()).isNotNull();
        assertThat(candidate.creatorReference().creatorFarmId()).isEqualTo(2L);
        assertThat(candidate.creatorReference().creatorNameSnapshot()).isEqualTo("Capril B");
        assertThat(candidate.ownershipHistory()).hasSize(1);
        assertThat(candidate.ownershipHistory().getFirst().farmId()).isEqualTo(1L);
        assertThat(candidate.ownershipHistory().getFirst().isOpen()).isTrue();
    }
}
