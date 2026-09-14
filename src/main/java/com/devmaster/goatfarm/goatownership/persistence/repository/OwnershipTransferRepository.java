package com.devmaster.goatfarm.goatownership.persistence.repository;

import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Collection;

public interface OwnershipTransferRepository extends JpaRepository<OwnershipTransferEntity, Long> {
    @Query("select transfer.goatId from OwnershipTransferEntity transfer where transfer.id = :transferId")
    Optional<Long> findGoatIdByTransferId(@Param("transferId") Long transferId);

    Optional<OwnershipTransferEntity> findByGoatIdAndStatusIn(Long goatId,
                                                               Collection<OwnershipTransferStatus> statuses);

    Optional<OwnershipTransferEntity> findByRequestedByAndIdempotencyKey(Long requestedBy, String idempotencyKey);
}
