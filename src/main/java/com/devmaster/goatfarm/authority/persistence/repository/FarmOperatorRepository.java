package com.devmaster.goatfarm.authority.persistence.repository;

import com.devmaster.goatfarm.authority.persistence.entity.FarmOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FarmOperatorRepository extends JpaRepository<FarmOperator, Long> {
    boolean existsByFarmIdAndUserId(Long farmId, Long userId);
}
