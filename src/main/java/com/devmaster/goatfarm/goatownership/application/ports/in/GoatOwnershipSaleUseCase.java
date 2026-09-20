package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipSaleRequest;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import java.util.Optional;

/** Canonical ownership boundary for the commercial internal-sale workflow. */
public interface GoatOwnershipSaleUseCase {
    OwnershipTransfer requestInternalSale(InternalOwnershipSaleRequest request);
    OwnershipTransfer findSaleTransfer(Long saleId);
    Optional<OwnershipTransfer> findByRequesterAndIdempotencyKey(Long requesterId, String idempotencyKey);
    OwnershipTransfer acceptInternalSale(Long saleId, boolean paymentConfirmed);
    OwnershipTransfer completeInternalSaleAfterPayment(Long saleId);
    OwnershipTransfer rejectInternalSale(Long saleId);
    OwnershipTransfer cancelInternalSale(Long saleId);
}
