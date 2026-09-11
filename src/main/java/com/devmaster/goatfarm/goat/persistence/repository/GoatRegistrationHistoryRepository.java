package com.devmaster.goatfarm.goat.persistence.repository;

import com.devmaster.goatfarm.goat.persistence.entity.GoatRegistrationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoatRegistrationHistoryRepository extends JpaRepository<GoatRegistrationHistoryEntity, Long> {

    List<GoatRegistrationHistoryEntity> findByFarmIdAndGoatIdOrderByCreatedAtDescIdDesc(Long farmId, Long goatId);
}
