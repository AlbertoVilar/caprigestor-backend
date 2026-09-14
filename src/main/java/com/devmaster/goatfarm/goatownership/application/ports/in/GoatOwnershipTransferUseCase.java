package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipTransferRequest;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;

/** Inbound application boundary for the internal ownership transfer workflow. */
public interface GoatOwnershipTransferUseCase {
    OwnershipTransfer requestInternalTransfer(InternalOwnershipTransferRequest request);

    OwnershipTransfer acceptTransfer(Long transferId);

    OwnershipTransfer rejectTransfer(Long transferId);

    OwnershipTransfer cancelTransfer(Long transferId);
}
