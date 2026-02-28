package com.devmaster.goatfarm.inventory.persistence.repository;

import com.devmaster.goatfarm.inventory.persistence.entity.InventoryBalanceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(
            value = """
                    select new com.devmaster.goatfarm.inventory.persistence.repository.InventoryBalanceQueryRow(
                        b.itemId,
                        i.name,
                        i.trackLot,
                        b.lotId,
                        b.quantity
                    )
                    from InventoryBalanceEntity b
                    join InventoryItemEntity i
                      on i.id = b.itemId
                     and i.farmId = b.farmId
                    where b.farmId = :farmId
                      and (:itemId is null or b.itemId = :itemId)
                      and (:lotId is null or b.lotId = :lotId)
                      and (:activeOnly = false or i.active = true)
                    """,
            countQuery = """
                    select count(b)
                    from InventoryBalanceEntity b
                    join InventoryItemEntity i
                      on i.id = b.itemId
                     and i.farmId = b.farmId
                    where b.farmId = :farmId
                      and (:itemId is null or b.itemId = :itemId)
                      and (:lotId is null or b.lotId = :lotId)
                      and (:activeOnly = false or i.active = true)
                    """
    )
    Page<InventoryBalanceQueryRow> searchBalances(
            @Param("farmId") Long farmId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable
    );
}
