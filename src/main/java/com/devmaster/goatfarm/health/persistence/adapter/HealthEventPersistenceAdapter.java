package com.devmaster.goatfarm.health.persistence.adapter;

import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import com.devmaster.goatfarm.health.persistence.repository.HealthEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HealthEventPersistenceAdapter implements HealthEventPersistencePort {

    private final HealthEventRepository repository;

    @Override
    public HealthEvent save(HealthEvent healthEvent) {
        return repository.save(healthEvent);
    }

    @Override
    public Optional<HealthEvent> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId) {
        return repository.findByIdAndFarmIdAndGoatId(id, farmId, goatId);
    }
}
