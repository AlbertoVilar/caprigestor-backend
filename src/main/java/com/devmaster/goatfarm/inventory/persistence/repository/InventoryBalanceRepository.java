package com.devmaster.goatfarm.inventory.persistence.repository;

import com.devmaster.goatfarm.inventory.persistence.entity.InventoryBalanceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalanceEntity, Long> {

    @Query("""
            select b
            from InventoryBalanceEntity b
            where b.farmId = :farmId
              and b.itemId = :itemId
              and (
                    (:lotId is null and b.lotId is null)
                    or b.lotId = :lotId
                  )
            """)
    Optional<InventoryBalanceEntity> findByBusinessKey(
            @Param("farmId") Long farmId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select b
            from InventoryBalanceEntity b
            where b.farmId = :farmId
              and b.itemId = :itemId
              and (
                    (:lotId is null and b.lotId is null)
                    or b.lotId = :lotId
                  )
            """)
    Optional<InventoryBalanceEntity> findByBusinessKeyForUpdate(
            @Param("farmId") Long farmId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId
    );
}
