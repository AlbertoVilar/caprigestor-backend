package com.devmaster.goatfarm.goatownership.persistence.repository;

import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Collection;

public interface OwnershipTransferRepository extends JpaRepository<OwnershipTransferEntity, Long> {
    @Query("select transfer.goatId from OwnershipTransferEntity transfer where transfer.id = :transferId")
    Optional<Long> findGoatIdByTransferId(@Param("transferId") Long transferId);

    Optional<OwnershipTransferEntity> findByGoatIdAndStatusIn(Long goatId,
                                                               Collection<OwnershipTransferStatus> statuses);

    boolean existsByGoatIdAndSourceFarmIdAndKindAndStatusIn(Long goatId, Long sourceFarmId,
                                                              OwnershipTransferKind kind,
                                                              Collection<OwnershipTransferStatus> statuses);

    Optional<OwnershipTransferEntity> findByRequestedByAndIdempotencyKey(Long requestedBy, String idempotencyKey);

    Optional<OwnershipTransferEntity> findBySaleId(Long saleId);

    @Query("select transfer from OwnershipTransferEntity transfer "
            + "where transfer.kind = :kind and transfer.targetFarmId = :farmId "
            + "and (:status is null or transfer.status = :status) "
            + "order by transfer.requestedAt desc, transfer.id desc")
    Page<OwnershipTransferEntity> findIncoming(@Param("farmId") Long farmId,
                                               @Param("kind") OwnershipTransferKind kind,
                                               @Param("status") OwnershipTransferStatus status,
                                               Pageable pageable);

    @Query("select transfer from OwnershipTransferEntity transfer "
            + "where transfer.kind = :kind and transfer.sourceFarmId = :farmId "
            + "and (:status is null or transfer.status = :status) "
            + "order by transfer.requestedAt desc, transfer.id desc")
    Page<OwnershipTransferEntity> findOutgoing(@Param("farmId") Long farmId,
                                               @Param("kind") OwnershipTransferKind kind,
                                               @Param("status") OwnershipTransferStatus status,
                                               Pageable pageable);
}
