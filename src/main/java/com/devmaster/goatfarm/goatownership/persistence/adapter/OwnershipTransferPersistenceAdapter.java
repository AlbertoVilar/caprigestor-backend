package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferQueryPort;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferPageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import com.devmaster.goatfarm.goatownership.persistence.repository.OwnershipTransferRepository;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

@Component
public class OwnershipTransferPersistenceAdapter implements OwnershipTransferPersistencePort, OwnershipTransferQueryPort {
    private static final List<OwnershipTransferStatus> PENDING = List.of(
            OwnershipTransferStatus.REQUESTED, OwnershipTransferStatus.ACCEPTED);

    private final OwnershipTransferRepository repository;
    private final OwnershipPersistenceMapper mapper;

    public OwnershipTransferPersistenceAdapter(OwnershipTransferRepository repository,
                                               OwnershipPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public OwnershipTransfer save(OwnershipTransfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("transfer is required");
        }
        OwnershipTransferEntity entity;
        if (transfer.id() == null) {
            entity = mapper.toEntity(transfer);
        } else {
            entity = repository.findById(transfer.id())
                    .orElseThrow(() -> new IllegalArgumentException("ownership transfer does not exist: " + transfer.id()));
            mapper.update(entity, transfer);
        }
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<GoatId> findGoatIdByTransferId(Long transferId) {
        return transferId == null ? Optional.empty()
                : repository.findGoatIdByTransferId(transferId).map(GoatId::of);
    }

    @Override
    public Optional<OwnershipTransfer> findById(Long transferId) {
        return transferId == null ? Optional.empty() : repository.findById(transferId).map(mapper::toDomain);
    }

    @Override
    public Optional<OwnershipTransfer> findBySaleId(Long saleId) {
        return saleId == null ? Optional.empty() : repository.findBySaleId(saleId).map(mapper::toDomain);
    }

    @Override
    public Optional<OwnershipTransfer> findPendingByGoatId(GoatId goatId) {
        return goatId == null ? Optional.empty()
                : repository.findByGoatIdAndStatusIn(goatId.value(), PENDING).map(mapper::toDomain);
    }

    @Override
    public Optional<OwnershipTransfer> findByRequesterAndIdempotencyKey(Long requestedBy, String idempotencyKey) {
        return requestedBy == null || idempotencyKey == null ? Optional.empty()
                : repository.findByRequestedByAndIdempotencyKey(requestedBy, idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public boolean existsByGoatIdAndSourceFarmIdAndKindAndStatusIn(GoatId goatId, Long sourceFarmId,
                                                                    OwnershipTransferKind kind,
                                                                    Collection<OwnershipTransferStatus> statuses) {
        return goatId != null && sourceFarmId != null && kind != null && statuses != null
                && repository.existsByGoatIdAndSourceFarmIdAndKindAndStatusIn(goatId.value(), sourceFarmId, kind, statuses);
    }

    @Override
    public PageResult<OwnershipTransfer> findForFarm(Long farmId,
                                                      OwnershipTransferDirection direction,
                                                      OwnershipTransferStatus status,
                                                      OwnershipTransferPageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());
        var page = direction == OwnershipTransferDirection.INCOMING
                ? repository.findIncoming(farmId, OwnershipTransferKind.INTERNAL_TRANSFER, status, pageable)
                : repository.findOutgoing(farmId, OwnershipTransferKind.INTERNAL_TRANSFER, status, pageable);
        return new PageResult<>(page.getContent().stream().map(mapper::toDomain).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
