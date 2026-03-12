package com.devmaster.goatfarm.inventory.application.ports.out;

import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface InventoryLotPersistencePort {

    InventoryLotResponseVO save(InventoryLotCreateVO lot);

    Page<InventoryLotResponseVO> listLots(InventoryLotFilterVO filter);

    Optional<InventoryLotResponseVO> findByFarmIdAndId(Long farmId, Long lotId);

    Optional<InventoryLotResponseVO> findByFarmIdAndItemIdAndCodeNormalized(Long farmId, Long itemId, String codeNormalized);

    Optional<InventoryItemSnapshotVO> findItemSnapshot(Long farmId, Long itemId);

    Optional<InventoryLotResponseVO> updateActive(Long farmId, Long lotId, boolean active);
}
