package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class ReproductiveEventPersistenceAdapter implements ReproductiveEventPersistencePort {

    private final ReproductiveEventRepository repository;

    public ReproductiveEventPersistenceAdapter(ReproductiveEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public ReproductiveEvent save(ReproductiveEvent entity) {
        return repository.save(entity);
    }

    @Override
    public Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        return repository.findAllByFarmIdAndGoatIdOrderByEventDateDesc(farmId, goatId, pageable);
    }

    @Override
    public Optional<ReproductiveEvent> findLatestCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date) {
        return repository.findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDesc(
                farmId,
                goatId,
                ReproductiveEventType.COVERAGE,
                date
        );
    }
}
