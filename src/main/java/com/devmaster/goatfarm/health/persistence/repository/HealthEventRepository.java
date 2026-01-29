package com.devmaster.goatfarm.health.persistence.repository;

import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthEventRepository extends JpaRepository<HealthEvent, Long> {
}
