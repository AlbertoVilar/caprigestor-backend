package com.devmaster.goatfarm.inventory.persistence.repository;

import com.devmaster.goatfarm.inventory.persistence.entity.InventoryMovementEntity;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovementEntity, Long> {

    @Query(
            value = """
                    select new com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementQueryRow(
                        m.id,
                        m.type,
                        m.adjustDirection,
                        m.quantity,
                        m.itemId,
                        i.name,
                        m.lotId,
                        m.movementDate,
                        m.reason,
                        m.resultingBalance,
                        m.createdAt
                    )
                    from InventoryMovementEntity m
                    join InventoryItemEntity i
                      on i.id = m.itemId
                     and i.farmId = m.farmId
                    where m.farmId = :farmId
                      and (:itemId is null or m.itemId = :itemId)
                      and (:lotId is null or m.lotId = :lotId)
                      and (:type is null or m.type = :type)
                      and (:fromDate is null or m.movementDate >= :fromDate)
                      and (:toDate is null or m.movementDate <= :toDate)
                    """,
            countQuery = """
                    select count(m)
                    from InventoryMovementEntity m
                    join InventoryItemEntity i
                      on i.id = m.itemId
                     and i.farmId = m.farmId
                    where m.farmId = :farmId
                      and (:itemId is null or m.itemId = :itemId)
                      and (:lotId is null or m.lotId = :lotId)
                      and (:type is null or m.type = :type)
                      and (:fromDate is null or m.movementDate >= :fromDate)
                      and (:toDate is null or m.movementDate <= :toDate)
                    """
    )
    Page<InventoryMovementQueryRow> searchMovements(
            @Param("farmId") Long farmId,
            @Param("itemId") Long itemId,
            @Param("lotId") Long lotId,
            @Param("type") InventoryMovementType type,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}
