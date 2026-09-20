package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipLockState;
import com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipTransferRequest;
import com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipSaleRequest;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipTransferUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipSaleUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatCurrentOwnerProjectionPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipLockPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Transactional application core for consented local ownership transfers.
 * Framework-specific authorization and persistence details remain behind
 * application ports.
 */
@Service
public class GoatOwnershipTransferBusiness implements GoatOwnershipTransferUseCase, GoatOwnershipSaleUseCase {
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;
    private final FarmAuthorizationUseCase farmAuthorization;
    private final GoatFarmPersistencePort farmPersistence;
    private final GoatOwnershipLockPort ownershipLock;
    private final GoatOwnershipPeriodPersistencePort periodPersistence;
    private final OwnershipTransferPersistencePort transferPersistence;
    private final GoatCurrentOwnerProjectionPort projection;
    private final Clock clock;

    public GoatOwnershipTransferBusiness(CurrentPrincipalQueryUseCase currentPrincipalQuery,
                                         FarmAuthorizationUseCase farmAuthorization,
                                         GoatFarmPersistencePort farmPersistence,
                                         GoatOwnershipLockPort ownershipLock,
                                         GoatOwnershipPeriodPersistencePort periodPersistence,
                                         OwnershipTransferPersistencePort transferPersistence,
                                         GoatCurrentOwnerProjectionPort projection,
                                         Clock clock) {
        this.currentPrincipalQuery = currentPrincipalQuery;
        this.farmAuthorization = farmAuthorization;
        this.farmPersistence = farmPersistence;
        this.ownershipLock = ownershipLock;
        this.periodPersistence = periodPersistence;
        this.transferPersistence = transferPersistence;
        this.projection = projection;
        this.clock = clock;
    }

    @Override
    @Transactional
    public OwnershipTransfer requestInternalTransfer(InternalOwnershipTransferRequest request) {
        validateRequest(request);
        AuthenticatedPrincipal principal = currentPrincipalQuery.requireCurrent();
        long requesterId = requirePrincipalId(principal);
        String reason = normalizeReason(request.reason());
        String idempotencyKey = normalizeKey(request.idempotencyKey());

        OwnershipTransfer existing = transferPersistence
                .findByRequesterAndIdempotencyKey(requesterId, idempotencyKey)
                .orElse(null);
        if (existing != null) {
            return resolveIdempotent(existing, request.goatId(), request.targetFarmId(), reason);
        }

        GoatOwnershipLockState lock = lockGoat(request.goatId());
        existing = transferPersistence
                .findByRequesterAndIdempotencyKey(requesterId, idempotencyKey)
                .orElse(null);
        if (existing != null) {
            return resolveIdempotent(existing, request.goatId(), request.targetFarmId(), reason);
        }

        GoatOwnershipPeriod source = requireOpenPeriod(lock);
        long sourceFarmId = source.farmId();
        validateTargetFarm(sourceFarmId, request.targetFarmId());
        requireCanAdminister(sourceFarmId);
        farmPersistence.findById(request.targetFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Target farm not found: " + request.targetFarmId()));
        transferPersistence.findPendingByGoatId(request.goatId()).ifPresent(pending -> {
            throw new BusinessRuleException("another ownership transfer is already pending for this GoatId");
        });

        OwnershipTransfer transfer = OwnershipTransfer.request(
                request.goatId(), sourceFarmId, request.targetFarmId(),
                OwnershipTransferKind.INTERNAL_TRANSFER, reason, idempotencyKey,
                Instant.now(clock), requesterId, null);
        return transferPersistence.save(transfer);
    }

    @Override
    @Transactional
    public OwnershipTransfer acceptTransfer(Long transferId) {
        GoatId goatId = requireGoatId(transferId);
        GoatOwnershipLockState lock = lockGoat(goatId);
        OwnershipTransfer transfer = requireTransfer(transferId);
        requireInternalTransfer(transfer);
        requireCanAdminister(transfer.targetFarmId());

        if (transfer.status() == OwnershipTransferStatus.COMPLETED) {
            validateCompletedTransfer(transfer, lock);
            return transfer;
        }
        if (transfer.status() != OwnershipTransferStatus.REQUESTED) {
            throw new BusinessRuleException("only a requested transfer can be accepted");
        }

        GoatOwnershipPeriod source = requireOpenPeriod(lock);
        if (!Objects.equals((long) source.farmId(), transfer.sourceFarmId())) {
            throw new BusinessRuleException("canonical source ownership no longer matches the transfer");
        }
        validateTargetFarm(source.farmId(), transfer.targetFarmId());
        long targetFarmId = transfer.targetFarmId();
        farmPersistence.findById(targetFarmId)
                .orElseThrow(() -> new ResourceNotFoundException("Target farm not found: " + targetFarmId));

        AuthenticatedPrincipal principal = currentPrincipalQuery.requireCurrent();
        long actorId = requirePrincipalId(principal);
        Instant effectiveAt = Instant.now(clock);
        source.close(effectiveAt, OwnershipExitType.TRANSFER_OUT);
        GoatOwnershipPeriod target = GoatOwnershipPeriod.open(
                transfer.goatId(), targetFarmId, effectiveAt,
                OwnershipEntryType.TRANSFER_IN, "OWNERSHIP_TRANSFER:" + transfer.id());

        List<GoatOwnershipPeriod> history = new ArrayList<>(periodPersistence.findByGoatIdOrderByStartedAt(transfer.goatId()));
        replacePeriod(history, source);
        history.add(target);
        GoatOwnershipPeriod.ensureConsistent(history);

        periodPersistence.handoff(source, target);
        if (!projection.moveFromTo(transfer.goatId(), source.farmId(), transfer.targetFarmId())) {
            throw new BusinessRuleException("current-owner projection drift detected; transfer rolled back");
        }
        transfer.acceptAndComplete(effectiveAt, effectiveAt, actorId, effectiveAt);
        return transferPersistence.save(transfer);
    }

    @Override
    @Transactional
    public OwnershipTransfer rejectTransfer(Long transferId) {
        GoatId goatId = requireGoatId(transferId);
        GoatOwnershipLockState lock = lockGoat(goatId);
        OwnershipTransfer transfer = requireTransfer(transferId);
        requireInternalTransfer(transfer);
        requireCanAdminister(transfer.targetFarmId());
        if (transfer.status() == OwnershipTransferStatus.REJECTED) {
            return transfer;
        }
        if (transfer.status() != OwnershipTransferStatus.REQUESTED) {
            throw new BusinessRuleException("only a requested transfer can be rejected");
        }
        requireOpenPeriod(lock);
        transfer.reject();
        return transferPersistence.save(transfer);
    }

    @Override
    @Transactional
    public OwnershipTransfer cancelTransfer(Long transferId) {
        GoatId goatId = requireGoatId(transferId);
        GoatOwnershipLockState lock = lockGoat(goatId);
        OwnershipTransfer transfer = requireTransfer(transferId);
        requireInternalTransfer(transfer);
        requireCanAdminister(transfer.sourceFarmId());
        if (transfer.status() == OwnershipTransferStatus.CANCELLED) {
            return transfer;
        }
        if (transfer.status() != OwnershipTransferStatus.REQUESTED) {
            throw new BusinessRuleException("only a requested transfer can be cancelled");
        }
        requireOpenPeriod(lock);
        transfer.cancel(Instant.now(clock));
        return transferPersistence.save(transfer);
    }

    @Override
    @Transactional
    public OwnershipTransfer requestInternalSale(InternalOwnershipSaleRequest request) {
        validateSaleRequest(request);
        AuthenticatedPrincipal principal = currentPrincipalQuery.requireCurrent();
        long requesterId = requirePrincipalId(principal);
        String key = normalizeKey(request.idempotencyKey());
        OwnershipTransfer existing = transferPersistence.findByRequesterAndIdempotencyKey(requesterId, key).orElse(null);
        if (existing != null) return resolveSaleIdempotent(existing, request);

        GoatOwnershipLockState lock = lockGoat(request.goatId());
        existing = transferPersistence.findByRequesterAndIdempotencyKey(requesterId, key).orElse(null);
        if (existing != null) return resolveSaleIdempotent(existing, request);
        GoatOwnershipPeriod source = requireOpenPeriod(lock);
        if (!Objects.equals(source.farmId(), request.sourceFarmId())) {
            throw new BusinessRuleException("canonical source ownership no longer matches the sale");
        }
        validateTargetFarm(source.farmId(), request.targetFarmId());
        requireCanAdminister(source.farmId());
        farmPersistence.findById(request.targetFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Target farm not found: " + request.targetFarmId()));
        transferPersistence.findPendingByGoatId(request.goatId()).ifPresent(pending -> {
            throw new BusinessRuleException("another ownership process is already pending for this GoatId");
        });
        OwnershipTransfer transfer = OwnershipTransfer.request(request.goatId(), request.sourceFarmId(), request.targetFarmId(),
                OwnershipTransferKind.INTERNAL_SALE, request.reason(), key, Instant.now(clock), requesterId, request.saleId());
        return transferPersistence.save(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public OwnershipTransfer findSaleTransfer(Long saleId) {
        OwnershipTransfer transfer = transferPersistence.findBySaleId(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Ownership sale transfer not found: " + saleId));
        if (transfer.kind() != OwnershipTransferKind.INTERNAL_SALE) {
            throw new ResourceNotFoundException("Ownership sale transfer not found: " + saleId);
        }
        return transfer;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OwnershipTransfer> findByRequesterAndIdempotencyKey(Long requesterId, String idempotencyKey) {
        return transferPersistence.findByRequesterAndIdempotencyKey(requesterId, normalizeKey(idempotencyKey))
                .filter(transfer -> transfer.kind() == OwnershipTransferKind.INTERNAL_SALE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveInternalSale(Long sourceFarmId, GoatId goatId) {
        return transferPersistence.existsByGoatIdAndSourceFarmIdAndKindAndStatusIn(
                goatId, sourceFarmId, OwnershipTransferKind.INTERNAL_SALE,
                List.of(OwnershipTransferStatus.REQUESTED, OwnershipTransferStatus.ACCEPTED));
    }

    @Override
    @Transactional
    public void prepareInternalSale(Long sourceFarmId, GoatId goatId) {
        GoatOwnershipLockState lock = lockGoat(goatId);
        GoatOwnershipPeriod source = requireOpenPeriod(lock);
        if (!Objects.equals((long) source.farmId(), sourceFarmId)) {
            throw new BusinessRuleException("canonical source ownership no longer matches the sale");
        }
        requireCanAdminister(sourceFarmId);
    }

    @Override
    @Transactional
    public OwnershipTransfer acceptInternalSale(Long saleId, boolean paymentConfirmed) {
        OwnershipTransfer transfer = lockAndReloadSale(saleId);
        requireCanAdminister(transfer.targetFarmId());
        if (transfer.status() == OwnershipTransferStatus.COMPLETED) return transfer;
        if (transfer.status() == OwnershipTransferStatus.ACCEPTED) {
            return paymentConfirmed ? completeSaleAfterAcceptance(transfer) : transfer;
        }
        if (transfer.status() != OwnershipTransferStatus.REQUESTED) {
            throw new BusinessRuleException("only a requested ownership sale can be accepted");
        }
        AuthenticatedPrincipal principal = currentPrincipalQuery.requireCurrent();
        transfer.markAccepted(Instant.now(clock), requirePrincipalId(principal));
        transfer = transferPersistence.save(transfer);
        return paymentConfirmed ? completeSaleAfterAcceptance(transfer) : transfer;
    }

    @Override
    @Transactional
    public OwnershipTransfer completeInternalSaleAfterPayment(Long saleId) {
        OwnershipTransfer transfer = lockAndReloadSale(saleId);
        if (transfer.status() == OwnershipTransferStatus.COMPLETED) return transfer;
        if (transfer.status() != OwnershipTransferStatus.ACCEPTED) return transfer;
        requireCanAdminister(transfer.targetFarmId());
        return completeSaleAfterAcceptance(transfer);
    }

    @Override
    @Transactional
    public OwnershipTransfer rejectInternalSale(Long saleId) {
        OwnershipTransfer transfer = lockAndReloadSale(saleId);
        requireCanAdminister(transfer.targetFarmId());
        if (transfer.status() == OwnershipTransferStatus.REJECTED) return transfer;
        if (transfer.status() != OwnershipTransferStatus.REQUESTED) {
            throw new BusinessRuleException("only a requested ownership sale can be rejected");
        }
        transfer.reject();
        return transferPersistence.save(transfer);
    }

    @Override
    @Transactional
    public OwnershipTransfer cancelInternalSale(Long saleId) {
        OwnershipTransfer transfer = lockAndReloadSale(saleId);
        requireCanAdminister(transfer.sourceFarmId());
        if (transfer.status() == OwnershipTransferStatus.CANCELLED) return transfer;
        if (transfer.status() != OwnershipTransferStatus.REQUESTED) {
            throw new BusinessRuleException("only a requested ownership sale can be cancelled");
        }
        transfer.cancel(Instant.now(clock));
        return transferPersistence.save(transfer);
    }

    private OwnershipTransfer lockAndReloadSale(Long saleId) {
        OwnershipTransfer snapshot = findSaleTransfer(saleId);
        lockGoat(snapshot.goatId());
        return findSaleTransfer(saleId);
    }

    private OwnershipTransfer completeSaleAfterAcceptance(OwnershipTransfer transfer) {
        GoatOwnershipLockState lock = lockGoat(transfer.goatId());
        GoatOwnershipPeriod source = requireOpenPeriod(lock);
        if (!Objects.equals((long) source.farmId(), transfer.sourceFarmId())) {
            throw new BusinessRuleException("canonical source ownership no longer matches the sale");
        }
        long actorId = requirePrincipalId(currentPrincipalQuery.requireCurrent());
        Instant effectiveAt = Instant.now(clock);
        source.close(effectiveAt, OwnershipExitType.EXTERNAL_SALE);
        GoatOwnershipPeriod target = GoatOwnershipPeriod.open(transfer.goatId(), transfer.targetFarmId(), effectiveAt,
                OwnershipEntryType.PURCHASE, "OWNERSHIP_SALE:" + transfer.saleId());
        List<GoatOwnershipPeriod> history = new ArrayList<>(periodPersistence.findByGoatIdOrderByStartedAt(transfer.goatId()));
        replacePeriod(history, source);
        history.add(target);
        GoatOwnershipPeriod.ensureConsistent(history);
        periodPersistence.handoff(source, target);
        if (!projection.moveFromTo(transfer.goatId(), source.farmId(), transfer.targetFarmId())) {
            throw new BusinessRuleException("current-owner projection drift detected; ownership sale rolled back");
        }
        transfer.completeAfterAcceptance(effectiveAt, actorId, effectiveAt);
        return transferPersistence.save(transfer);
    }

    private OwnershipTransfer resolveSaleIdempotent(OwnershipTransfer existing, InternalOwnershipSaleRequest request) {
        if (existing.kind() != OwnershipTransferKind.INTERNAL_SALE
                || !Objects.equals(existing.sourceFarmId(), request.sourceFarmId())
                || existing.targetFarmId() != request.targetFarmId()
                || !existing.goatId().equals(request.goatId())) {
            throw new BusinessRuleException("idempotency key already represents a different ownership sale");
        }
        return existing;
    }

    private void validateSaleRequest(InternalOwnershipSaleRequest request) {
        if (request == null || request.goatId() == null) throw new InvalidArgumentException("goatId is required");
        if (request.sourceFarmId() == null || request.sourceFarmId() <= 0) throw new InvalidArgumentException("sourceFarmId must be positive");
        if (request.targetFarmId() == null || request.targetFarmId() <= 0) throw new InvalidArgumentException("targetFarmId must be positive");
        if (request.saleId() == null || request.saleId() <= 0) throw new InvalidArgumentException("saleId must be positive");
        if (request.reason() == null || request.reason().trim().isEmpty()) throw new InvalidArgumentException("reason must not be blank");
        if (request.idempotencyKey() == null || request.idempotencyKey().trim().isEmpty()) throw new InvalidArgumentException("idempotencyKey must not be blank");
    }

    private GoatOwnershipLockState lockGoat(GoatId goatId) {
        return ownershipLock.lockGoatOwnership(goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Goat not found: " + goatId));
    }

    private OwnershipTransfer requireTransfer(Long transferId) {
        if (transferId == null || transferId <= 0) {
            throw new InvalidArgumentException("transferId must be positive");
        }
        return transferPersistence.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Ownership transfer not found: " + transferId));
    }

    private GoatId requireGoatId(Long transferId) {
        if (transferId == null || transferId <= 0) {
            throw new InvalidArgumentException("transferId must be positive");
        }
        return transferPersistence.findGoatIdByTransferId(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Ownership transfer not found: " + transferId));
    }

    private GoatOwnershipPeriod requireOpenPeriod(GoatOwnershipLockState lock) {
        return lock.openPeriod().orElseThrow(() -> new BusinessRuleException("Goat has no canonical open ownership period"));
    }

    private void requireInternalTransfer(OwnershipTransfer transfer) {
        if (transfer.kind() != OwnershipTransferKind.INTERNAL_TRANSFER) {
            throw new BusinessRuleException("only INTERNAL_TRANSFER is active in W6");
        }
    }

    private void requireCanAdminister(long farmId) {
        if (!farmAuthorization.canAdministerFarm(farmId)) {
            throw new AuthorizationDeniedException("current principal cannot administer farm " + farmId);
        }
    }

    private void validateTargetFarm(long sourceFarmId, Long targetFarmId) {
        if (targetFarmId == null || targetFarmId <= 0) {
            throw new InvalidArgumentException("targetFarmId must be positive");
        }
        if (sourceFarmId == targetFarmId) {
            throw new BusinessRuleException("source and target farms must differ");
        }
    }

    private void validateRequest(InternalOwnershipTransferRequest request) {
        if (request == null || request.goatId() == null) {
            throw new InvalidArgumentException("goatId is required");
        }
        if (request.targetFarmId() == null || request.targetFarmId() <= 0) {
            throw new InvalidArgumentException("targetFarmId must be positive");
        }
        if (request.reason() == null || request.reason().trim().isEmpty()) {
            throw new InvalidArgumentException("reason must not be blank");
        }
        if (request.idempotencyKey() == null || request.idempotencyKey().trim().isEmpty()) {
            throw new InvalidArgumentException("idempotencyKey must not be blank");
        }
    }

    private OwnershipTransfer resolveIdempotent(OwnershipTransfer existing, GoatId goatId,
                                                Long targetFarmId, String reason) {
        boolean same = existing.goatId().equals(goatId)
                && existing.targetFarmId() == targetFarmId
                && existing.kind() == OwnershipTransferKind.INTERNAL_TRANSFER
                && existing.saleId() == null
                && existing.reason().equals(reason);
        if (!same) {
            throw new BusinessRuleException("idempotency key already represents a different transfer request");
        }
        return existing;
    }

    private void replacePeriod(List<GoatOwnershipPeriod> history, GoatOwnershipPeriod replacement) {
        for (int i = 0; i < history.size(); i++) {
            GoatOwnershipPeriod candidate = history.get(i);
            if (candidate.id() != null && candidate.id().equals(replacement.id())) {
                history.set(i, replacement);
                return;
            }
        }
        throw new BusinessRuleException("locked open ownership period is absent from ownership history");
    }

    private void validateCompletedTransfer(OwnershipTransfer transfer, GoatOwnershipLockState lock) {
        GoatOwnershipPeriod current = requireOpenPeriod(lock);
        if (current.farmId() != transfer.targetFarmId()) {
            throw new BusinessRuleException("completed transfer does not match canonical current ownership");
        }
        GoatOwnershipPeriod.ensureConsistent(periodPersistence.findByGoatIdOrderByStartedAt(transfer.goatId()));
    }

    private long requirePrincipalId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.id() == null || principal.id() <= 0) {
            throw new AuthorizationDeniedException("current principal has no valid id");
        }
        return principal.id();
    }

    private String normalizeReason(String reason) {
        return reason == null ? null : reason.trim();
    }

    private String normalizeKey(String key) {
        return key == null ? null : key.trim();
    }
}
