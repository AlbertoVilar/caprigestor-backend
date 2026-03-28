package com.devmaster.goatfarm.audit.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.application.ports.out.OperationalAuditPersistencePort;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditEntryVO;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
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
    private final OwnershipService ownershipService;
    private final EntityFinder entityFinder;

    public OperationalAuditBusiness(
            OperationalAuditPersistencePort operationalAuditPersistencePort,
            GoatFarmPersistencePort goatFarmPersistencePort,
            OwnershipService ownershipService,
            EntityFinder entityFinder
    ) {
        this.operationalAuditPersistencePort = operationalAuditPersistencePort;
        this.goatFarmPersistencePort = goatFarmPersistencePort;
        this.ownershipService = ownershipService;
        this.entityFinder = entityFinder;
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
        User currentUser = ownershipService.getCurrentUser();

        operationalAuditPersistencePort.save(OperationalAuditEntry.builder()
                .farm(farm)
                .goatRegistrationNumber(normalizeOptionalText(recordVO.goatRegistrationNumber()))
                .actionType(recordVO.actionType())
                .targetId(normalizeOptionalText(recordVO.targetId()))
                .actorUserId(currentUser.getId())
                .actorName(currentUser.getName())
                .actorEmail(currentUser.getEmail())
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
                : operationalAuditPersistencePort.findByFarmIdAndGoatRegistrationNumber(farmId, normalizedGoatId, normalizedLimit);

        return entries.stream()
                .map(this::toVO)
                .toList();
    }

    private GoatFarm requireFarm(Long farmId) {
        return entityFinder.findOrThrow(
                () -> goatFarmPersistencePort.findById(farmId),
                "Fazenda nao encontrada."
        );
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
}
