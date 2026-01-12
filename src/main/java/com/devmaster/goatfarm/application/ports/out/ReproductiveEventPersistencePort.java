package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.reproduction.model.entity.ReproductiveEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReproductiveEventPersistencePort {
    ReproductiveEvent save(ReproductiveEvent entity);
    Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);
}
