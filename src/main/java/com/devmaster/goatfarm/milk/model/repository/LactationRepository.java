package com.devmaster.goatfarm.milk.model.repository;

import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LactationRepository extends JpaRepository<Lactation, Long> {

    Optional<Lactation> findByFarmIdAndGoatIdAndStatus(
            Long farmId,
            String goatId,
            LactationStatus status
    );

    Optional<Lactation> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);

    Page<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);
}
