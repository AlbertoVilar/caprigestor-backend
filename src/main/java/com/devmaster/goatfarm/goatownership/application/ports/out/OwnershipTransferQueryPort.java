package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferPageQuery;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

/** Persistence query boundary for authorized ownership transfer read models. */
public interface OwnershipTransferQueryPort {
    PageResult<OwnershipTransfer> findForFarm(Long farmId,
                                               OwnershipTransferDirection direction,
                                               OwnershipTransferStatus status,
                                               OwnershipTransferPageQuery pageQuery);
}
