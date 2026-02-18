package com.devmaster.goatfarm.inventory.persistence.repository;

import com.devmaster.goatfarm.inventory.persistence.entity.InventoryIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryIdempotencyRepository extends JpaRepository<InventoryIdempotencyEntity, Long> {
    Optional<InventoryIdempotencyEntity> findByFarmIdAndIdempotencyKey(Long farmId, String idempotencyKey);
}
