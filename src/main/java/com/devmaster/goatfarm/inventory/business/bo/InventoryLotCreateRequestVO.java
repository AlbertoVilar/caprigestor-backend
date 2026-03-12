package com.devmaster.goatfarm.inventory.business.bo;

import java.time.LocalDate;

public record InventoryLotCreateRequestVO(
        Long itemId,
        String code,
        String description,
        LocalDate expirationDate,
        Boolean active
) {
}
