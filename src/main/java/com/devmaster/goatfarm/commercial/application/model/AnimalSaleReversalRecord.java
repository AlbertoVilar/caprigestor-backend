package com.devmaster.goatfarm.commercial.application.model;

import java.time.LocalDateTime;

public record AnimalSaleReversalRecord(Long id, Long saleId, String reason,
                                       LocalDateTime reversedAt, Long reversedBy) {
}
