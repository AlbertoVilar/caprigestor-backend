package com.devmaster.goatfarm.goatownership.persistence.repository;

import com.devmaster.goatfarm.goatownership.persistence.entity.CreatorReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorReferenceRepository extends JpaRepository<CreatorReferenceEntity, Long> {
}
