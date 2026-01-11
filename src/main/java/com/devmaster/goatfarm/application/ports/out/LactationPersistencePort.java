package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.milk.model.entity.Lactation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LactationPersistencePort {

    Lactation save(Lactation lactation);

    Optional<Lactation> findActiveByFarmIdAndGoatId(Long farmId, String goatId);

    Optional<Lactation> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);

    Page<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);
}
