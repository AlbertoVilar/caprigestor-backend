package com.devmaster.goatfarm.goatownership.persistence;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.domain.CreatorSource;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferPageQuery;
import com.devmaster.goatfarm.goatownership.persistence.adapter.CreatorReferencePersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.adapter.GoatOwnershipLockPersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.adapter.GoatOwnershipPeriodPersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.adapter.GoatOwnershipQueryPersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.entity.CreatorReferenceEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import com.devmaster.goatfarm.goatownership.persistence.repository.CreatorReferenceRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipLockRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipPersistenceAdaptersTest {

    private static final GoatId GOAT_ID = GoatId.of(42L);
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private final OwnershipPersistenceMapper mapper = new OwnershipPersistenceMapper();

    @Mock
    private CreatorReferenceRepository creatorRepository;
    @Mock
    private GoatOwnershipPeriodRepository periodRepository;
    @Mock
    private GoatOwnershipLockRepository lockRepository;
    @Mock
    private com.devmaster.goatfarm.goatownership.persistence.repository.OwnershipTransferRepository transferRepository;

    @Test
    void creatorReference_roundTripsFarmExternalAndUnknownSourcesWithoutLosingSnapshots() {
        assertCreatorRoundTrip(CreatorReference.farm(" 12345 ", 7L, "  Capril Vilar ",
                CreatorSource.BIRTH, " DOC-1 ", START));
        assertCreatorRoundTrip(CreatorReference.external("98765", "External", CreatorSource.ABCC,
                "ABCC-1", START));
        assertCreatorRoundTrip(CreatorReference.unknown(START));
    }

    @Test
    void creatorReference_rejectsDuplicateAndNeverExposesUpdateOperation() {
        when(creatorRepository.existsById(GOAT_ID.value())).thenReturn(true);
        var adapter = new CreatorReferencePersistenceAdapter(creatorRepository, mapper);
        assertThatThrownBy(() -> adapter.create(GOAT_ID, CreatorReference.unknown(START)))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(creatorRepository).existsById(GOAT_ID.value());
    }

    @Test
    void ownershipQueryUsesOpenPeriodAndExactHalfOpenInterval() {
        var entity = periodEntity(11L, 8L, START, null);
        when(periodRepository.findByGoatIdAndEndedAtIsNull(42L)).thenReturn(Optional.of(entity));
        when(periodRepository.existsOwnedByFarmAt(42L, 8L, START)).thenReturn(true);
        var adapter = new GoatOwnershipQueryPersistenceAdapter(periodRepository, mapper);

        assertThat(adapter.findCurrentOwnerFarmId(GOAT_ID)).contains(8L);
        assertThat(adapter.isOwnedByFarmAt(GOAT_ID, 8L, START)).isTrue();
        assertThat(adapter.isOwnedByFarmAt(GOAT_ID, 8L, null)).isFalse();
        verify(periodRepository).existsOwnedByFarmAt(42L, 8L, START);
    }

    @Test
    void lockReturnsExistingGoatWithEmptyPeriodAndUsesCanonicalOrder() {
        when(lockRepository.lockGoatById(42L)).thenReturn(Optional.of(42L));
        when(periodRepository.findOpenForUpdateByGoatId(42L)).thenReturn(Optional.empty());
        var adapter = new GoatOwnershipLockPersistenceAdapter(lockRepository, periodRepository, mapper);

        var state = adapter.lockGoatOwnership(GOAT_ID).orElseThrow();
        assertThat(state.goatId()).isEqualTo(GOAT_ID);
        assertThat(state.openPeriod()).isEmpty();
        InOrder order = inOrder(lockRepository, periodRepository);
        order.verify(lockRepository).lockGoatById(42L);
        order.verify(periodRepository).findOpenForUpdateByGoatId(42L);
    }

    @Test
    void lockReturnsEmptyForMissingGoat() {
        when(lockRepository.lockGoatById(42L)).thenReturn(Optional.empty());
        var adapter = new GoatOwnershipLockPersistenceAdapter(lockRepository, periodRepository, mapper);
        assertThat(adapter.lockGoatOwnership(GOAT_ID)).isEmpty();
    }

    @Test
    void ownershipHandoffClosesSourceBeforeInsertingTarget() {
        var sourceEntity = periodEntity(11L, 8L, START, START.plusSeconds(60));
        sourceEntity.setExitType(com.devmaster.goatfarm.goatownership.domain.OwnershipExitType.TRANSFER_OUT);
        var target = GoatOwnershipPeriod.open(GOAT_ID, 9L, START.plusSeconds(60),
                OwnershipEntryType.TRANSFER_IN, "OWNERSHIP_TRANSFER:3");
        when(periodRepository.findById(11L)).thenReturn(Optional.of(sourceEntity));
        var targetEntity = mapper.toEntity(target);
        when(periodRepository.save(any(GoatOwnershipPeriodEntity.class))).thenReturn(targetEntity);
        var adapter = new GoatOwnershipPeriodPersistenceAdapter(periodRepository, mapper);

        adapter.handoff(GoatOwnershipPeriod.rehydrate(11L, GOAT_ID, 8L, START, START.plusSeconds(60),
                OwnershipEntryType.PURCHASE, com.devmaster.goatfarm.goatownership.domain.OwnershipExitType.TRANSFER_OUT, "test"), target);

        InOrder order = inOrder(periodRepository);
        order.verify(periodRepository).findById(11L);
        order.verify(periodRepository).saveAndFlush(sourceEntity);
        order.verify(periodRepository).save(any(GoatOwnershipPeriodEntity.class));
    }

    @Test
    void transferMapperRehydratesEveryLifecycleState() {
        for (var status : OwnershipTransferStatus.values()) {
            var entity = transferEntity(status);
            var domain = mapper.toDomain(entity);
            assertThat(domain.status()).isEqualTo(status);
            assertThat(mapper.toEntity(domain).getVersion()).isNull();
        }
    }

    @Test
    void transferQueryAdapterUsesInternalKindDirectionStatusAndNeutralPage() {
        var entity = transferEntity(OwnershipTransferStatus.REQUESTED);
        when(transferRepository.findIncoming(2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.REQUESTED, PageRequest.of(1, 10)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(1, 10), 11));
        var adapter = new com.devmaster.goatfarm.goatownership.persistence.adapter.OwnershipTransferPersistenceAdapter(
                transferRepository, mapper);

        var result = adapter.findForFarm(2L, OwnershipTransferDirection.INCOMING,
                OwnershipTransferStatus.REQUESTED, new OwnershipTransferPageQuery(1, 10));

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(11);
        assertThat(result.content()).hasSize(1);
        verify(transferRepository).findIncoming(2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.REQUESTED, PageRequest.of(1, 10));
    }

    @Test
    void persistenceEntitiesDeclareOptimisticVersionWithoutLeakingItIntoDomain() throws Exception {
        assertThat(GoatOwnershipPeriodEntity.class.getDeclaredField("version").isAnnotationPresent(jakarta.persistence.Version.class)).isTrue();
        assertThat(OwnershipTransferEntity.class.getDeclaredField("version").isAnnotationPresent(jakarta.persistence.Version.class)).isTrue();
        assertThat(GoatOwnershipPeriod.class.getDeclaredFields()).extracting(Field::getName).doesNotContain("version");
        assertThat(OwnershipTransfer.class.getDeclaredFields()).extracting(Field::getName).doesNotContain("version");
    }

    private void assertCreatorRoundTrip(CreatorReference creator) {
        CreatorReferenceEntity entity = mapper.toEntity(GOAT_ID, creator);
        assertThat(mapper.toDomain(entity)).isEqualTo(creator);
    }

    private GoatOwnershipPeriodEntity periodEntity(Long id, long farmId, Instant startedAt, Instant endedAt) {
        var entity = new GoatOwnershipPeriodEntity();
        entity.setId(id);
        entity.setGoatId(GOAT_ID.value());
        entity.setFarmId(farmId);
        entity.setStartedAt(startedAt);
        entity.setEndedAt(endedAt);
        entity.setEntryType(OwnershipEntryType.PURCHASE);
        entity.setSource("test");
        return entity;
    }

    private OwnershipTransferEntity transferEntity(OwnershipTransferStatus status) {
        Instant accepted = status == OwnershipTransferStatus.ACCEPTED
                || status == OwnershipTransferStatus.COMPLETED || status == OwnershipTransferStatus.CANCELLED
                ? START.plusSeconds(60) : null;
        Instant completed = status == OwnershipTransferStatus.COMPLETED ? START.plusSeconds(120) : null;
        Instant cancelled = status == OwnershipTransferStatus.CANCELLED ? START.plusSeconds(120) : null;
        var entity = new OwnershipTransferEntity();
        entity.setId(3L);
        entity.setGoatId(GOAT_ID.value());
        entity.setSourceFarmId(1L);
        entity.setTargetFarmId(2L);
        entity.setKind(OwnershipTransferKind.INTERNAL_TRANSFER);
        entity.setStatus(status);
        entity.setReason("test");
        entity.setIdempotencyKey("key-" + status);
        entity.setRequestedAt(START);
        entity.setRequestedBy(10L);
        entity.setAcceptedAt(accepted);
        entity.setAcceptedBy(accepted == null ? null : 10L);
        entity.setEffectiveAt(completed);
        entity.setCompletedAt(completed);
        entity.setCompletedBy(completed == null ? null : 10L);
        entity.setCancelledAt(cancelled);
        return entity;
    }
}
