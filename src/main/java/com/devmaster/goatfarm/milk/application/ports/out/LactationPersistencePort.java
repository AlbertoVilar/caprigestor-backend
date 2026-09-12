package com.devmaster.goatfarm.milk.application.ports.out;

import com.devmaster.goatfarm.milk.domain.Lactation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface LactationPersistencePort {

    Lactation save(Lactation lactation);

    Optional<Lactation> findActiveByFarmIdAndGoatId(Long farmId, String goatId);

    Optional<Lactation> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);

    Page<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);

    List<Lactation> findAllActiveByFarmId(Long farmId);
}
