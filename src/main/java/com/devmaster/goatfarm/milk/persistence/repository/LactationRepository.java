package com.devmaster.goatfarm.milk.persistence.repository;

import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LactationRepository extends JpaRepository<LactationEntity, Long> {
    Optional<LactationEntity> findByFarmIdAndGoatIdAndStatus(Long farmId, String goatId, LactationStatus status);
    Optional<LactationEntity> findByFarmIdAndGoatTechnicalIdAndStatus(Long farmId, Long goatTechnicalId, LactationStatus status);
    Optional<LactationEntity> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    Optional<LactationEntity> findByIdAndFarmIdAndGoatTechnicalId(Long id, Long farmId, Long goatTechnicalId);
    Page<LactationEntity> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);
    Page<LactationEntity> findAllByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId, Pageable pageable);
    List<LactationEntity> findAllByFarmIdAndStatus(Long farmId, LactationStatus status);
}
