package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryIdempotencyVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InventoryMovementPersistenceAdapter implements InventoryMovementPersistencePort {

    @Override
    public Optional<InventoryIdempotencyVO> findIdempotency(Long farmId, String idempotencyKey) {
        // TODO: implementar busca por (farmId, idempotencyKey) na persistência.
        return Optional.empty();
    }

    @Override
    public InventoryIdempotencyVO saveIdempotency(InventoryIdempotencyVO vo) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public InventoryMovementPersistedVO saveMovement(InventoryMovementPersistedVO vo) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public InventoryBalanceSnapshotVO lockBalanceForUpdate(Long farmId, Long itemId, Long lotId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public InventoryBalanceSnapshotVO upsertBalance(InventoryBalanceSnapshotVO vo) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
