package com.devmaster.goatfarm.audit.persistence.repository;

import com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationalAuditEntryRepository extends JpaRepository<OperationalAuditEntry, Long> {

    List<OperationalAuditEntry> findByFarm_IdOrderByCreatedAtDescIdDesc(Long farmId, Pageable pageable);

    List<OperationalAuditEntry> findByFarm_IdAndGoatRegistrationNumberOrderByCreatedAtDescIdDesc(
            Long farmId,
            String goatRegistrationNumber,
            Pageable pageable
    );
}
