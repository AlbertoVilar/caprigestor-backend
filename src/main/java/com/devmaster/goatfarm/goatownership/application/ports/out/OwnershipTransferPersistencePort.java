package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;

import java.util.Optional;
import java.util.Collection;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

public interface OwnershipTransferPersistencePort {
    OwnershipTransfer save(OwnershipTransfer transfer);

    Optional<GoatId> findGoatIdByTransferId(Long transferId);

    Optional<OwnershipTransfer> findById(Long transferId);

    Optional<OwnershipTransfer> findBySaleId(Long saleId);

    Optional<GoatId> findGoatIdBySaleId(Long saleId);

    Optional<OwnershipTransfer> findPendingByGoatId(GoatId goatId);

    Optional<OwnershipTransfer> findByRequesterAndIdempotencyKey(Long requestedBy, String idempotencyKey);

    boolean existsByGoatIdAndSourceFarmIdAndKindAndStatusIn(GoatId goatId, Long sourceFarmId,
                                                             OwnershipTransferKind kind,
                                                             Collection<OwnershipTransferStatus> statuses);
}
