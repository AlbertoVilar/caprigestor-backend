package com.devmaster.goatfarm.milk.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Framework-free read model for a farm daily milk consolidation. */
public record FarmMilkProduction(
        Long id,
        Long farmId,
        LocalDate productionDate,
        BigDecimal totalProduced,
        BigDecimal withdrawalProduced,
        BigDecimal marketableProduced,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public LocalDate getProductionDate() { return productionDate; }
    public BigDecimal getTotalProduced() { return totalProduced; }
    public BigDecimal getWithdrawalProduced() { return withdrawalProduced; }
    public BigDecimal getMarketableProduced() { return marketableProduced; }
    public String getNotes() { return notes; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
