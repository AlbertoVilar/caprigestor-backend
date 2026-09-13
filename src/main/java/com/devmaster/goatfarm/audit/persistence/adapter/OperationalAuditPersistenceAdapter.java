package com.devmaster.goatfarm.audit.persistence.adapter;

import com.devmaster.goatfarm.audit.application.ports.out.OperationalAuditPersistencePort;
import com.devmaster.goatfarm.audit.application.model.OperationalAuditRecord;
import com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry;
import com.devmaster.goatfarm.audit.persistence.repository.OperationalAuditEntryRepository;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OperationalAuditPersistenceAdapter implements OperationalAuditPersistencePort {

    private final OperationalAuditEntryRepository operationalAuditEntryRepository;
    private final GoatFarmRepository goatFarmRepository;

    public OperationalAuditPersistenceAdapter(OperationalAuditEntryRepository operationalAuditEntryRepository,
                                               GoatFarmRepository goatFarmRepository) {
        this.operationalAuditEntryRepository = operationalAuditEntryRepository;
        this.goatFarmRepository = goatFarmRepository;
    }

    @Override
    public OperationalAuditRecord save(OperationalAuditRecord record) {
        OperationalAuditEntry entry = toEntity(record);
        OperationalAuditEntry saved = operationalAuditEntryRepository.save(entry);
        return toRecord(saved);
    }

    @Override
    public List<OperationalAuditRecord> findByFarmId(Long farmId, int limit) {
        return operationalAuditEntryRepository.findByFarm_IdOrderByCreatedAtDescIdDesc(farmId, PageRequest.of(0, limit))
                .stream().map(this::toRecord).toList();
    }

    @Override
    public List<OperationalAuditRecord> findByFarmIdAndGoatRegistrationNumber(Long farmId, String goatRegistrationNumber, int limit) {
        return operationalAuditEntryRepository.findByFarm_IdAndGoatRegistrationNumberOrderByCreatedAtDescIdDesc(
                farmId,
                goatRegistrationNumber,
                PageRequest.of(0, limit)
        ).stream().map(this::toRecord).toList();
    }

    @Override
    public List<OperationalAuditRecord> findByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId, int limit) {
        return operationalAuditEntryRepository.findByFarm_IdAndGoatTechnicalIdOrderByCreatedAtDescIdDesc(
                farmId, goatTechnicalId, PageRequest.of(0, limit)).stream().map(this::toRecord).toList();
    }

    private OperationalAuditEntry toEntity(OperationalAuditRecord record) {
        OperationalAuditEntry entry = OperationalAuditEntry.builder()
                .id(record.id())
                .farm(goatFarmRepository.getReferenceById(record.farmId()))
                .goatTechnicalId(record.goatTechnicalId())
                .goatRegistrationNumber(record.goatRegistrationNumber())
                .actionType(record.actionType())
                .targetId(record.targetId())
                .actorUserId(record.actorUserId())
                .actorName(record.actorName())
                .actorEmail(record.actorEmail())
                .description(record.description())
                .createdAt(record.createdAt())
                .build();
        return entry;
    }

    private OperationalAuditRecord toRecord(OperationalAuditEntry entry) {
        return new OperationalAuditRecord(
                entry.getId(),
                entry.getFarm() == null ? null : entry.getFarm().getId(),
                entry.getGoatTechnicalId(),
                entry.getGoatRegistrationNumber(),
                entry.getActionType(),
                entry.getTargetId(),
                entry.getActorUserId(),
                entry.getActorName(),
                entry.getActorEmail(),
                entry.getDescription(),
                entry.getCreatedAt()
        );
    }
}
