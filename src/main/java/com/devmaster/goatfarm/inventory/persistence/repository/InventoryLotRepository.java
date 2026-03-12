package com.devmaster.goatfarm.inventory.persistence.repository;

import com.devmaster.goatfarm.inventory.persistence.entity.InventoryLotEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryLotRepository extends JpaRepository<InventoryLotEntity, Long> {

    Optional<InventoryLotEntity> findByFarmIdAndId(Long farmId, Long id);

    Optional<InventoryLotEntity> findByFarmIdAndItemIdAndCodeNormalized(Long farmId, Long itemId, String codeNormalized);

    @Query("""
            select lot
            from InventoryLotEntity lot
            where lot.farmId = :farmId
              and (:itemId is null or lot.itemId = :itemId)
              and (:active is null or lot.active = :active)
            """)
    Page<InventoryLotEntity> searchLots(
            @Param("farmId") Long farmId,
            @Param("itemId") Long itemId,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
