package com.devmaster.goatfarm.commercial.application.ports.out;

import com.devmaster.goatfarm.commercial.application.model.AnimalSaleReversalRecord;

import java.util.Optional;

public interface AnimalSaleReversalPersistencePort {
    AnimalSaleReversalRecord save(Long saleId, String reason, java.time.LocalDateTime reversedAt, Long reversedBy);
    Optional<AnimalSaleReversalRecord> findBySaleId(Long saleId);
}
