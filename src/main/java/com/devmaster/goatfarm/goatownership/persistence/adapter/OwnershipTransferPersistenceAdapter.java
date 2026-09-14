package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import com.devmaster.goatfarm.goatownership.persistence.mapper.OwnershipPersistenceMapper;
import com.devmaster.goatfarm.goatownership.persistence.repository.OwnershipTransferRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OwnershipTransferPersistenceAdapter implements OwnershipTransferPersistencePort {
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
    public Optional<OwnershipTransfer> findPendingByGoatId(GoatId goatId) {
        return goatId == null ? Optional.empty()
                : repository.findByGoatIdAndStatusIn(goatId.value(), PENDING).map(mapper::toDomain);
    }

    @Override
    public Optional<OwnershipTransfer> findByRequesterAndIdempotencyKey(Long requestedBy, String idempotencyKey) {
        return requestedBy == null || idempotencyKey == null ? Optional.empty()
                : repository.findByRequestedByAndIdempotencyKey(requestedBy, idempotencyKey).map(mapper::toDomain);
    }
}
