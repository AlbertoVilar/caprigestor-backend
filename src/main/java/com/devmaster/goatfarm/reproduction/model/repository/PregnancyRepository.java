package com.devmaster.goatfarm.reproduction.model.repository;

import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.model.entity.Pregnancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PregnancyRepository extends JpaRepository<Pregnancy, Long> {
    Optional<Pregnancy> findByFarmIdAndGoatIdAndStatus(Long farmId, String goatId, PregnancyStatus status);
    Optional<Pregnancy> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    Page<Pregnancy> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);
}
