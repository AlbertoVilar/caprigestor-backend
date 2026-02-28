package com.devmaster.goatfarm.inventory.business.bo;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public record InventoryMovementFilterVO(
        Long farmId,
        Long itemId,
        Long lotId,
        InventoryMovementType type,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
) {
}
