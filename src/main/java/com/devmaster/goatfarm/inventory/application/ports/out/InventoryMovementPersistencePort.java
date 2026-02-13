package com.devmaster.goatfarm.inventory.application.ports.out;

import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryIdempotencyVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO;

import java.util.Optional;

public interface InventoryMovementPersistencePort {

    Optional<InventoryIdempotencyVO> findIdempotency(Long farmId, String idempotencyKey);

    InventoryIdempotencyVO saveIdempotency(InventoryIdempotencyVO vo);

    InventoryMovementPersistedVO saveMovement(InventoryMovementPersistedVO vo);

    InventoryBalanceSnapshotVO lockBalanceForUpdate(Long farmId, Long itemId, Long lotId);

    InventoryBalanceSnapshotVO upsertBalance(InventoryBalanceSnapshotVO vo);
}
