package com.devmaster.goatfarm.goatownership.persistence.mapper;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.persistence.entity.CreatorReferenceEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import org.springframework.stereotype.Component;

/** Explicit boundary mapper; all rehydration goes through domain factories. */
@Component
public class OwnershipPersistenceMapper {
    public CreatorReferenceEntity toEntity(GoatId goatId, CreatorReference domain) {
        var entity = new CreatorReferenceEntity();
        entity.setGoatId(goatId.value());
        return update(entity, domain);
    }

    public CreatorReferenceEntity update(CreatorReferenceEntity entity, CreatorReference domain) {
        entity.setCreatorTod(domain.creatorTod());
        entity.setCreatorFarmId(domain.creatorFarmId());
        entity.setCreatorNameSnapshot(domain.creatorNameSnapshot());
        entity.setSource(domain.source());
        entity.setEvidenceReference(domain.evidenceReference());
        entity.setRecordedAt(domain.recordedAt());
        return entity;
    }

    public CreatorReference toDomain(CreatorReferenceEntity entity) {
        return new CreatorReference(entity.getCreatorTod(), entity.getCreatorFarmId(),
                entity.getCreatorNameSnapshot(), entity.getSource(), entity.getEvidenceReference(),
                entity.getRecordedAt());
    }

    public GoatOwnershipPeriodEntity toEntity(GoatOwnershipPeriod domain) {
        var entity = new GoatOwnershipPeriodEntity();
        entity.setId(domain.id());
        entity.setGoatId(domain.goatId().value());
        return update(entity, domain);
    }

    public GoatOwnershipPeriodEntity update(GoatOwnershipPeriodEntity entity, GoatOwnershipPeriod domain) {
        entity.setGoatId(domain.goatId().value());
        entity.setFarmId(domain.farmId());
        entity.setStartedAt(domain.startedAt());
        entity.setEndedAt(domain.endedAt());
        entity.setEntryType(domain.entryType());
        entity.setExitType(domain.exitType());
        entity.setSource(domain.source());
        return entity;
    }

    public GoatOwnershipPeriod toDomain(GoatOwnershipPeriodEntity entity) {
        return GoatOwnershipPeriod.rehydrate(entity.getId(), GoatId.of(entity.getGoatId()), entity.getFarmId(),
                entity.getStartedAt(), entity.getEndedAt(), entity.getEntryType(), entity.getExitType(), entity.getSource());
    }

    public OwnershipTransferEntity toEntity(OwnershipTransfer domain) {
        var entity = new OwnershipTransferEntity();
        entity.setId(domain.id());
        entity.setGoatId(domain.goatId().value());
        return update(entity, domain);
    }

    public OwnershipTransferEntity update(OwnershipTransferEntity entity, OwnershipTransfer domain) {
        entity.setGoatId(domain.goatId().value());
        entity.setSourceFarmId(domain.sourceFarmId());
        entity.setTargetFarmId(domain.targetFarmId());
        entity.setKind(domain.kind());
        entity.setStatus(domain.status());
        entity.setReason(domain.reason());
        entity.setIdempotencyKey(domain.idempotencyKey());
        entity.setRequestedAt(domain.requestedAt());
        entity.setAcceptedAt(domain.acceptedAt());
        entity.setEffectiveAt(domain.effectiveAt());
        entity.setCompletedAt(domain.completedAt());
        entity.setCancelledAt(domain.cancelledAt());
        entity.setRequestedBy(domain.requestedBy());
        entity.setAcceptedBy(domain.acceptedBy());
        entity.setCompletedBy(domain.completedBy());
        entity.setSaleId(domain.saleId());
        return entity;
    }

    public OwnershipTransfer toDomain(OwnershipTransferEntity entity) {
        return OwnershipTransfer.rehydrate(entity.getId(), GoatId.of(entity.getGoatId()), entity.getSourceFarmId(),
                entity.getTargetFarmId(), entity.getKind(), entity.getStatus(), entity.getReason(),
                entity.getIdempotencyKey(), entity.getRequestedAt(), entity.getAcceptedAt(), entity.getEffectiveAt(),
                entity.getCompletedAt(), entity.getCancelledAt(), entity.getRequestedBy(), entity.getAcceptedBy(),
                entity.getCompletedBy(), entity.getSaleId());
    }
}
