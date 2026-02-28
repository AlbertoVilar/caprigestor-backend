package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementQueryPort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementQueryPersistenceAdapter implements InventoryMovementQueryPort {

    private final InventoryMovementRepository movementRepository;

    public InventoryMovementQueryPersistenceAdapter(InventoryMovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Override
    public Page<InventoryMovementHistoryResponseVO> listMovements(InventoryMovementFilterVO filter) {
        return movementRepository.searchMovements(
                filter.farmId(),
                filter.itemId(),
                filter.lotId(),
                filter.type(),
                filter.fromDate(),
                filter.toDate(),
                filter.pageable()
        ).map(row -> new InventoryMovementHistoryResponseVO(
                row.movementId(),
                row.type(),
                row.adjustDirection(),
                row.quantity(),
                row.itemId(),
                row.itemName(),
                row.lotId(),
                row.movementDate(),
                row.reason(),
                row.resultingBalance(),
                row.createdAt()
        ));
    }
}
