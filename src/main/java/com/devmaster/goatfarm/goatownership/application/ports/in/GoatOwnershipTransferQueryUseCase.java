package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferPageQuery;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

/** Authorized read boundary for the internal ownership transfer workflow. */
public interface GoatOwnershipTransferQueryUseCase {
    OwnershipTransfer findAuthorizedById(Long transferId);

    PageResult<OwnershipTransfer> listForFarm(Long farmId,
                                               OwnershipTransferDirection direction,
                                               OwnershipTransferStatus status,
                                               OwnershipTransferPageQuery pageQuery);
}
