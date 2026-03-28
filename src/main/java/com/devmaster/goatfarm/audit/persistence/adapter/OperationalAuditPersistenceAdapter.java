package com.devmaster.goatfarm.audit.persistence.adapter;

import com.devmaster.goatfarm.audit.application.ports.out.OperationalAuditPersistencePort;
import com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry;
import com.devmaster.goatfarm.audit.persistence.repository.OperationalAuditEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OperationalAuditPersistenceAdapter implements OperationalAuditPersistencePort {

    private final OperationalAuditEntryRepository operationalAuditEntryRepository;

    public OperationalAuditPersistenceAdapter(OperationalAuditEntryRepository operationalAuditEntryRepository) {
        this.operationalAuditEntryRepository = operationalAuditEntryRepository;
    }

    @Override
    public OperationalAuditEntry save(OperationalAuditEntry entry) {
        return operationalAuditEntryRepository.save(entry);
    }

    @Override
    public List<OperationalAuditEntry> findByFarmId(Long farmId, int limit) {
        return operationalAuditEntryRepository.findByFarm_IdOrderByCreatedAtDescIdDesc(farmId, PageRequest.of(0, limit));
    }

    @Override
    public List<OperationalAuditEntry> findByFarmIdAndGoatRegistrationNumber(Long farmId, String goatRegistrationNumber, int limit) {
        return operationalAuditEntryRepository.findByFarm_IdAndGoatRegistrationNumberOrderByCreatedAtDescIdDesc(
                farmId,
                goatRegistrationNumber,
                PageRequest.of(0, limit)
        );
    }
}
