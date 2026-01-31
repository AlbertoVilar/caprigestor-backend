package com.devmaster.goatfarm.health.application.ports.out;

import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import java.util.Optional;

public interface HealthEventPersistencePort {
    HealthEvent save(HealthEvent healthEvent);
    Optional<HealthEvent> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
}
