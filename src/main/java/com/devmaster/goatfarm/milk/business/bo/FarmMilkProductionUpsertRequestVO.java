package com.devmaster.goatfarm.milk.business.bo;

import java.math.BigDecimal;

public record FarmMilkProductionUpsertRequestVO(
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        String notes
) {
}
