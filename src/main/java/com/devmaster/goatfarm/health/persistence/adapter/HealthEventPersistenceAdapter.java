package com.devmaster.goatfarm.health.persistence.adapter;

import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class HealthEventPersistenceAdapter implements HealthEventPersistencePort {

    private final HealthEventRepository repository;

    public HealthEventPersistenceAdapter(HealthEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public HealthEvent save(HealthEvent healthEvent) {
        return repository.save(healthEvent);
    }

    @Override
    public Optional<HealthEvent> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId) {
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
}
