package com.devmaster.goatfarm.audit.application.ports.out;

import com.devmaster.goatfarm.audit.application.model.OperationalAuditRecord;

import java.util.List;

public interface OperationalAuditPersistencePort {

    OperationalAuditRecord save(OperationalAuditRecord record);

    List<OperationalAuditRecord> findByFarmId(Long farmId, int limit);

    List<OperationalAuditRecord> findByFarmIdAndGoatRegistrationNumber(Long farmId, String goatRegistrationNumber, int limit);

    List<OperationalAuditRecord> findByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId, int limit);
}
