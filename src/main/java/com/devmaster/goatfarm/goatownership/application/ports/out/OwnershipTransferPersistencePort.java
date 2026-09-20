package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;

import java.util.Optional;

public interface OwnershipTransferPersistencePort {
    OwnershipTransfer save(OwnershipTransfer transfer);

    Optional<GoatId> findGoatIdByTransferId(Long transferId);

    Optional<OwnershipTransfer> findById(Long transferId);

    Optional<OwnershipTransfer> findBySaleId(Long saleId);

    Optional<OwnershipTransfer> findPendingByGoatId(GoatId goatId);

    Optional<OwnershipTransfer> findByRequesterAndIdempotencyKey(Long requestedBy, String idempotencyKey);
}
