package com.devmaster.goatfarm.inventory.persistence.repository;

import com.devmaster.goatfarm.inventory.persistence.entity.InventoryItemEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, Long> {

    Optional<InventoryItemEntity> findByFarmIdAndId(Long farmId, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select i
            from InventoryItemEntity i
            where i.farmId = :farmId
              and i.id = :itemId
            """)
    Optional<InventoryItemEntity> findByFarmIdAndIdForUpdate(
            @Param("farmId") Long farmId,
            @Param("itemId") Long itemId
    );
}
