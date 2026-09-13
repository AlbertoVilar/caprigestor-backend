package com.devmaster.goatfarm.audit.persistence.adapter;

import com.devmaster.goatfarm.audit.application.ports.out.OperationalAuditPersistencePort;
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
    public OperationalAuditEntry save(OperationalAuditEntry entry) {
        if (entry.getFarm() != null && entry.getFarm().getId() != null) {
            entry.setFarm(goatFarmRepository.getReferenceById(entry.getFarm().getId()));
        }
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

    @Override
    public List<OperationalAuditEntry> findByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId, int limit) {
        return operationalAuditEntryRepository.findByFarm_IdAndGoatTechnicalIdOrderByCreatedAtDescIdDesc(
                farmId, goatTechnicalId, PageRequest.of(0, limit));
    }
}
