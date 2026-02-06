package com.devmaster.goatfarm.reproduction.persistence.repository;

import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PregnancyRepository extends JpaRepository<Pregnancy, Long> {
    List<Pregnancy> findByFarmIdAndGoatIdAndStatusOrderByBreedingDateDescIdDesc(Long farmId, String goatId, PregnancyStatus status);
    Optional<Pregnancy> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    Optional<Pregnancy> findByFarmIdAndId(Long farmId, Long id);
    Optional<Pregnancy> findByFarmIdAndCoverageEventId(Long farmId, Long coverageEventId);
    Page<Pregnancy> findAllByFarmIdAndGoatIdOrderByBreedingDateDescIdDesc(Long farmId, String goatId, Pageable pageable);
}
