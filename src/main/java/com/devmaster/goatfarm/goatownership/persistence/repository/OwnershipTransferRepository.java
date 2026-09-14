package com.devmaster.goatfarm.goatownership.persistence.repository;

import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;

public interface OwnershipTransferRepository extends JpaRepository<OwnershipTransferEntity, Long> {
    Optional<OwnershipTransferEntity> findByGoatIdAndStatusIn(Long goatId,
                                                               Collection<OwnershipTransferStatus> statuses);

    Optional<OwnershipTransferEntity> findByRequestedByAndIdempotencyKey(Long requestedBy, String idempotencyKey);
}
