package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.commercial.application.ports.out.InventoryPurchaseCostQueryPort;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class InventoryPurchaseCostQueryAdapter implements InventoryPurchaseCostQueryPort {

    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryPurchaseCostQueryAdapter(InventoryMovementRepository inventoryMovementRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @Override
    public BigDecimal sumPurchaseCostsByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate) {
        return inventoryMovementRepository.sumPurchaseCostsByFarmIdAndPeriod(farmId, fromDate, toDate);
    }
}
