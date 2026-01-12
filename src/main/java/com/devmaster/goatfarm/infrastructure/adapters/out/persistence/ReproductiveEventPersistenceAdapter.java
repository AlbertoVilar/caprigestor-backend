package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.model.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.model.repository.ReproductiveEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReproductiveEventPersistenceAdapter implements ReproductiveEventPersistencePort {

    private final ReproductiveEventRepository repository;

    @Override
    public ReproductiveEvent save(ReproductiveEvent entity) {
        return repository.save(entity);
    }

    @Override
    public Page<ReproductiveEvent> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        return repository.findAllByFarmIdAndGoatId(farmId, goatId, pageable);
    }
}
