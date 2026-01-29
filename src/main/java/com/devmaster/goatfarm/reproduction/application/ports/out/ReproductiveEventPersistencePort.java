package com.devmaster.goatfarm.reproduction.application.ports.out;

import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface ReproductiveEventPersistencePort {
    ReproductiveEvent save(ReproductiveEvent entity);

    Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);

    Optional<ReproductiveEvent> findLatestCoverageByFarmIdAndGoatIdOnOrBefore(Long farmId, String goatId, LocalDate date);
}
