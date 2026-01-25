package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import com.devmaster.goatfarm.milk.model.repository.LactationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LactationPersistenceAdapter implements LactationPersistencePort {

    private final LactationRepository lactationRepository;

    public LactationPersistenceAdapter(LactationRepository lactationRepository) {
        this.lactationRepository = lactationRepository;
    }

    @Override
    public Lactation save(Lactation lactation) {
        return lactationRepository.save(lactation);
    }

    @Override
    public Optional<Lactation> findActiveByFarmIdAndGoatId(Long farmId, String goatId) {
        return lactationRepository.findByFarmIdAndGoatIdAndStatus(farmId, goatId, LactationStatus.ACTIVE);
    }

    @Override
    public Optional<Lactation> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId) {
        return lactationRepository.findByIdAndFarmIdAndGoatId(id, farmId, goatId);
    }

    @Override
    public Page<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable) {
        return lactationRepository.findAllByFarmIdAndGoatId(farmId, goatId, pageable);
    }
}
