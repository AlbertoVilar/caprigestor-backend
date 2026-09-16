package com.devmaster.goatfarm.goatownership.persistence.repository;

import com.devmaster.goatfarm.goatownership.persistence.entity.CreatorReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CreatorReferenceRepository extends JpaRepository<CreatorReferenceEntity, Long> {
    List<CreatorReferenceEntity> findByCreatorFarmId(Long creatorFarmId);
    Optional<CreatorReferenceEntity> findByGoatId(Long goatId);
}
