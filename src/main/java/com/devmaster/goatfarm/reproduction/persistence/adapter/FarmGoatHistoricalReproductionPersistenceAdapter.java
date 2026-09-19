package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalReproductionEventItem;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalReproductionProcessCandidate;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalReproductionQueryPort;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEventEntity;
import com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Reproduction-owned persistence adapter that queries historical pregnancy and reproductive event records.
 * Returns technology-neutral application models implementing the goatownership query port.
 */
@Component
public class FarmGoatHistoricalReproductionPersistenceAdapter implements FarmGoatHistoricalReproductionQueryPort {

    private final PregnancyRepository pregnancyRepository;
    private final ReproductiveEventRepository reproductiveEventRepository;

    public FarmGoatHistoricalReproductionPersistenceAdapter(
            PregnancyRepository pregnancyRepository,
            ReproductiveEventRepository reproductiveEventRepository
    ) {
        this.pregnancyRepository = Objects.requireNonNull(pregnancyRepository, "pregnancyRepository must not be null");
        this.reproductiveEventRepository = Objects.requireNonNull(reproductiveEventRepository, "reproductiveEventRepository must not be null");
    }

    @Override
    public List<HistoricalReproductionProcessCandidate> findCandidateProcessesByGoat(GoatId goatId, String registrationNumber) {
        if (goatId == null) {
            throw new IllegalArgumentException("goatId must not be null");
        }

        List<PregnancyEntity> entities = pregnancyRepository.findHistoricalCandidates(
                goatId.value(),
                registrationNumber
        );

        return entities.stream()
                .map(p -> new HistoricalReproductionProcessCandidate(
                        p.getId(),
                        goatId,
                        p.getFarmId(),
                        p.getStatus(),
                        p.getBreedingDate(),
                        p.getConfirmDate(),
                        p.getExpectedDueDate(),
                        p.getClosedAt(),
                        p.getCloseReason(),
                        p.getCoverageEventId(),
                        p.getNotes()
                ))
                .toList();
    }

    @Override
    public List<HistoricalReproductionEventItem> findCandidateEventsByGoat(GoatId goatId, String registrationNumber) {
        if (goatId == null) {
            throw new IllegalArgumentException("goatId must not be null");
        }

        List<ReproductiveEventEntity> entities = reproductiveEventRepository.findHistoricalCandidates(
                goatId.value(),
                registrationNumber
        );

        return entities.stream()
                .map(e -> new HistoricalReproductionEventItem(
                        e.getId(),
                        goatId,
                        e.getFarmId(),
                        e.getEventType(),
                        e.getEventDate(),
                        e.getBreedingType(),
                        e.getBreederRef(),
                        e.getPregnancyId(),
                        e.getRelatedEventId(),
                        e.getCorrectedEventDate(),
                        e.getCheckScheduledDate(),
                        e.getCheckResult(),
                        e.getNotes()
                ))
                .toList();
    }
}
