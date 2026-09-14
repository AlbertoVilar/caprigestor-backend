package com.devmaster.goatfarm.goatownership.persistence;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.domain.CreatorSource;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.persistence.adapter.CreatorReferencePersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.adapter.GoatOwnershipLockPersistenceAdapter;
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

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
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
    void transferMapperRehydratesEveryLifecycleState() {
        for (var status : OwnershipTransferStatus.values()) {
            var entity = transferEntity(status);
            var domain = mapper.toDomain(entity);
            assertThat(domain.status()).isEqualTo(status);
            assertThat(mapper.toEntity(domain).getVersion()).isNull();
        }
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
