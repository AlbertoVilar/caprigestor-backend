package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.milk.model.entity.Lactation;

import java.util.Optional;

public interface LactationPersistencePort {

    Lactation save(Lactation lactation);

    Optional<Lactation> findActiveByFarmIdAndGoatId(Long farmId, String goatId);
}
