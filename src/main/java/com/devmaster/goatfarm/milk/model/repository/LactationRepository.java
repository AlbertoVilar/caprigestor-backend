package com.devmaster.goatfarm.milk.model.repository;

import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LactationRepository extends JpaRepository<Lactation, Long> {

    Optional<Lactation> findByFarmIdAndGoatIdAndStatus(
            Long farmId,
            String goatId,
            LactationStatus status
    );
}
