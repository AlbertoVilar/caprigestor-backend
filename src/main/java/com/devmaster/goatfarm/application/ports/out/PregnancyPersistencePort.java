package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.reproduction.model.entity.Pregnancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PregnancyPersistencePort {
    Pregnancy save(Pregnancy entity);
    Optional<Pregnancy> findActiveByFarmIdAndGoatId(Long farmId, String goatId);
    Optional<Pregnancy> findByIdAndFarmIdAndGoatId(Long pregnancyId, Long farmId, String goatId);
    Page<Pregnancy> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);
    List<Pregnancy> findAllActiveByFarmIdAndGoatIdOrdered(Long farmId, String goatId);
}
