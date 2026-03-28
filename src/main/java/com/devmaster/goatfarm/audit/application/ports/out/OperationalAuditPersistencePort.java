package com.devmaster.goatfarm.audit.application.ports.out;

import com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry;

import java.util.List;

public interface OperationalAuditPersistencePort {

    OperationalAuditEntry save(OperationalAuditEntry entry);

    List<OperationalAuditEntry> findByFarmId(Long farmId, int limit);

    List<OperationalAuditEntry> findByFarmIdAndGoatRegistrationNumber(Long farmId, String goatRegistrationNumber, int limit);
}
