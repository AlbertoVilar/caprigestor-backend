package com.devmaster.goatfarm.health.persistence.adapter;

import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class HealthEventPersistenceAdapter implements HealthEventPersistencePort {

    private final HealthEventRepository repository;
    private final GoatReferenceQueryPort goatReferenceQueryPort;

    public HealthEventPersistenceAdapter(HealthEventRepository repository) {
        this(repository, null);
    }

    @Autowired
    public HealthEventPersistenceAdapter(HealthEventRepository repository,
                                         GoatReferenceQueryPort goatReferenceQueryPort) {
        this.repository = repository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
    }

    @Override
    public HealthEvent save(HealthEvent healthEvent) {
        populateTechnicalIdentity(healthEvent);
        return repository.save(healthEvent);
    }

    @Override
    public Optional<HealthEvent> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Optional<HealthEvent> technical = repository.findByIdAndFarmIdAndGoatTechnicalId(id, farmId, technicalId.get());
            if (technical.isPresent()) {
                return technical;
            }
        }
        return repository.findByIdAndFarmIdAndGoatId(id, farmId, goatId);
    }

    @Override
    public Page<HealthEvent> findByFarmIdAndGoatId(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            HealthEventType type,
            HealthEventStatus status,
            Pageable pageable
    ) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            Page<HealthEvent> technical = repository.searchByGoatTechnicalId(farmId, technicalId.get(), from, to, type, status, pageable);
            if (technical.hasContent()) {
                return technical;
            }
        }
        return repository.searchByGoat(farmId, goatId, from, to, type, status, pageable);
    }

    @Override
    public Page<HealthEvent> findByFarmIdAndPeriod(
            Long farmId,
            LocalDate from,
            LocalDate to,
            HealthEventType type,
            HealthEventStatus status,
            Pageable pageable
    ) {
        return repository.searchCalendar(farmId, from, to, type, status, pageable);
    }

    @Override
    public List<HealthEvent> findPerformedWithWithdrawalByFarmIdAndGoatId(Long farmId, String goatId) {
        Optional<Long> technicalId = technicalId(farmId, goatId);
        if (technicalId.isPresent()) {
            List<HealthEvent> technical = repository.findPerformedWithWithdrawalByFarmIdAndGoatTechnicalId(farmId, technicalId.get());
            if (!technical.isEmpty()) {
                return technical;
            }
        }
        return repository.findPerformedWithWithdrawalByFarmIdAndGoatId(farmId, goatId);
    }

    @Override
    public List<HealthEvent> findPerformedWithWithdrawalByFarmId(Long farmId) {
        return repository.findPerformedWithWithdrawalByFarmId(farmId);
    }

    private Optional<Long> technicalId(Long farmId, String registrationNumber) {
        if (goatReferenceQueryPort == null || registrationNumber == null) {
            return Optional.empty();
        }
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .map(GoatReference::id)
                .map(id -> id.value());
    }

    private void populateTechnicalIdentity(HealthEvent entity) {
        if (entity.getGoatTechnicalId() == null) {
            technicalId(entity.getFarmId(), entity.getGoatId()).ifPresent(entity::setGoatTechnicalId);
        }
    }
}
