package com.devmaster.goatfarm.audit.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.application.ports.out.OperationalAuditPersistencePort;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditEntryVO;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OperationalAuditBusiness implements OperationalAuditUseCase {

    private static final int DEFAULT_LIMIT = 15;
    private static final int MAX_LIMIT = 50;

    private final OperationalAuditPersistencePort operationalAuditPersistencePort;
    private final GoatFarmPersistencePort goatFarmPersistencePort;
    private final FarmAuthorizationUseCase ownershipService;
    private final EntityFinder entityFinder;
    private final GoatReferenceQueryPort goatReferenceQueryPort;
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;

    @Autowired
    public OperationalAuditBusiness(
            OperationalAuditPersistencePort operationalAuditPersistencePort,
            GoatFarmPersistencePort goatFarmPersistencePort,
            FarmAuthorizationUseCase ownershipService,
            EntityFinder entityFinder,
            GoatReferenceQueryPort goatReferenceQueryPort,
            CurrentPrincipalQueryUseCase currentPrincipalQuery
    ) {
        this.operationalAuditPersistencePort = operationalAuditPersistencePort;
        this.goatFarmPersistencePort = goatFarmPersistencePort;
        this.ownershipService = ownershipService;
        this.entityFinder = entityFinder;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.currentPrincipalQuery = currentPrincipalQuery;
    }

    @Override
    @Transactional
    public void record(OperationalAuditRecordVO recordVO) {
        if (recordVO == null) {
            throw new InvalidArgumentException("audit", "Registro de auditoria e obrigatorio.");
        }
        if (recordVO.farmId() == null) {
            throw new InvalidArgumentException("farmId", "Fazenda da auditoria e obrigatoria.");
        }
        if (recordVO.actionType() == null) {
            throw new InvalidArgumentException("actionType", "Tipo de auditoria e obrigatorio.");
        }
        String description = normalizeRequiredText("description", recordVO.description(), "Descricao da auditoria e obrigatoria.");

        GoatFarm farm = requireFarm(recordVO.farmId());
        AuthenticatedPrincipal currentUser = currentPrincipalQuery.requireCurrent();

        operationalAuditPersistencePort.save(OperationalAuditEntry.builder()
                .farm(farm)
                .goatTechnicalId(resolveTechnicalId(recordVO))
                .goatRegistrationNumber(normalizeOptionalText(recordVO.goatRegistrationNumber()))
                .actionType(recordVO.actionType())
                .targetId(normalizeOptionalText(recordVO.targetId()))
                .actorUserId(currentUser.id())
                .actorName(currentUser.name())
                .actorEmail(currentUser.email())
                .description(description)
                .build());
    }

    @Override
    public List<OperationalAuditEntryVO> listEntries(Long farmId, String goatId, int limit) {
        requireFarm(farmId);

        int normalizedLimit = normalizeLimit(limit);
        String normalizedGoatId = normalizeOptionalText(goatId);

        List<OperationalAuditEntry> entries = normalizedGoatId == null
                ? operationalAuditPersistencePort.findByFarmId(farmId, normalizedLimit)
                : (goatReferenceQueryPort == null ? java.util.Optional.<GoatReference>empty()
                    : goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(normalizedGoatId, farmId))
                    .map(GoatReference::id)
                    .map(id -> operationalAuditPersistencePort.findByFarmIdAndGoatTechnicalId(farmId, id.value(), normalizedLimit))
                    .orElseGet(() -> operationalAuditPersistencePort.findByFarmIdAndGoatRegistrationNumber(farmId, normalizedGoatId, normalizedLimit));

        return entries.stream()
                .map(this::toVO)
                .toList();
    }

    private GoatFarm requireFarm(Long farmId) {
        java.util.Optional<?> optional = goatFarmPersistencePort.findById(farmId);
        if (optional.isEmpty()) throw new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Fazenda nao encontrada.");
        FarmRecord record = toRecord(optional.get());
        GoatFarm farm = new GoatFarm(); farm.setId(record.id()); return farm;
    }

    private FarmRecord toRecord(Object value) {
        if (value instanceof FarmRecord record) return record;
        GoatFarm legacy = (GoatFarm) value;
        return new FarmRecord(legacy.getId(), legacy.getName(), legacy.getTod(), legacy.getLogoUrl(), null, null, java.util.List.of(), legacy.getCreatedAt(), legacy.getUpdatedAt(), legacy.getVersion());
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeRequiredText(String fieldName, String value, String message) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new InvalidArgumentException(fieldName, message);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private OperationalAuditEntryVO toVO(OperationalAuditEntry entry) {
        return new OperationalAuditEntryVO(
                entry.getId(),
                entry.getGoatTechnicalId(),
                entry.getGoatRegistrationNumber(),
                entry.getActionType(),
                entry.getActionType().getLabel(),
                entry.getTargetId(),
                entry.getDescription(),
                entry.getActorUserId(),
                entry.getActorName(),
                entry.getActorEmail(),
                entry.getCreatedAt()
        );
    }

    private Long resolveTechnicalId(OperationalAuditRecordVO record) {
        if (record.goatTechnicalId() != null) {
            return record.goatTechnicalId();
        }
        if (record.goatRegistrationNumber() == null) {
            return null;
        }
        if (goatReferenceQueryPort == null) {
            return null;
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(
                record.goatRegistrationNumber(), record.farmId())
                .map(GoatReference::id)
                .map(id -> id.value())
                .orElse(null);
    }
}
