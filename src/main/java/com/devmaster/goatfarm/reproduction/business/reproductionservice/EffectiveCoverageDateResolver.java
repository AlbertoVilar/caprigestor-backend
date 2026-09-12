package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** Resolves a coverage's effective date, including its optional correction. */
@Service
public class EffectiveCoverageDateResolver {
    private final ReproductiveEventPersistencePort eventPersistencePort;

    public EffectiveCoverageDateResolver(ReproductiveEventPersistencePort eventPersistencePort) {
        this.eventPersistencePort = eventPersistencePort;
    }

    public LocalDate resolve(Long farmId, String goatId, ReproductiveEvent coverage) {
        return eventPersistencePort.findCoverageCorrectionByRelatedEventId(farmId, goatId, coverage.getId())
                .map(ReproductiveEvent::getCorrectedEventDate)
                .filter(date -> date != null)
                .orElse(coverage.getEventDate());
    }
}
