package com.devmaster.goatfarm.goatownership.domain;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.time.Instant;
import java.util.Objects;

/**
 * Explicit, consented transfer of one existing biological goat between farms.
 * Normal transfers may complete on acceptance. Internal sales may remain
 * ACCEPTED until the independent payment prerequisite is recorded.
 */
public final class OwnershipTransfer {
    private final Long id;
    private final GoatId goatId;
    private final Long sourceFarmId;
    private final long targetFarmId;
    private final OwnershipTransferKind kind;
    private final String reason;
    private final String idempotencyKey;
    private OwnershipTransferStatus status;
    private final Instant requestedAt;
    private Instant acceptedAt;
    private Instant effectiveAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private final long requestedBy;
    private Long acceptedBy;
    private Long completedBy;
    private Long saleId;

    private OwnershipTransfer(
            Long id,
            GoatId goatId,
            Long sourceFarmId,
            long targetFarmId,
            OwnershipTransferKind kind,
            OwnershipTransferStatus status,
            String reason,
            String idempotencyKey,
            Instant requestedAt,
            Instant acceptedAt,
            Instant effectiveAt,
            Instant completedAt,
            Instant cancelledAt,
            long requestedBy,
            Long acceptedBy,
            Long completedBy,
            Long saleId
    ) {
        this.id = id;
        this.goatId = Objects.requireNonNull(goatId, "goatId must not be null");
        this.sourceFarmId = sourceFarmId;
        if (sourceFarmId != null && sourceFarmId <= 0) {
            throw new IllegalArgumentException("sourceFarmId must be positive when provided");
        }
        if (targetFarmId <= 0) {
            throw new IllegalArgumentException("targetFarmId must be positive");
        }
        if (sourceFarmId != null && sourceFarmId == targetFarmId) {
            throw new IllegalArgumentException("source and target farms must differ");
        }
        this.targetFarmId = targetFarmId;
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        if (kind == OwnershipTransferKind.EXTERNAL_CLAIM && sourceFarmId != null) {
            throw new IllegalArgumentException("external claim must not have a source farm");
        }
        if (kind != OwnershipTransferKind.EXTERNAL_CLAIM && sourceFarmId == null) {
            throw new IllegalArgumentException("internal transfer must have a source farm");
        }
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.reason = requireText(reason, "reason");
        this.idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt must not be null");
        this.acceptedAt = acceptedAt;
        this.effectiveAt = effectiveAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        if (requestedBy <= 0) {
            throw new IllegalArgumentException("requestedBy must be positive");
        }
        this.requestedBy = requestedBy;
        this.acceptedBy = acceptedBy;
        this.completedBy = completedBy;
        this.saleId = saleId;
        validateTimestamps();
    }

    public static OwnershipTransfer request(
            GoatId goatId,
            Long sourceFarmId,
            long targetFarmId,
            OwnershipTransferKind kind,
            String reason,
            String idempotencyKey,
            Instant requestedAt,
            long requestedBy,
            Long saleId
    ) {
        return new OwnershipTransfer(null, goatId, sourceFarmId, targetFarmId, kind,
                OwnershipTransferStatus.REQUESTED, reason, idempotencyKey, requestedAt,
                null, null, null, null, requestedBy, null, null, saleId);
    }

    public static OwnershipTransfer rehydrate(
            Long id,
            GoatId goatId,
            Long sourceFarmId,
            long targetFarmId,
            OwnershipTransferKind kind,
            OwnershipTransferStatus status,
            String reason,
            String idempotencyKey,
            Instant requestedAt,
            Instant acceptedAt,
            Instant effectiveAt,
            Instant completedAt,
            Instant cancelledAt,
            long requestedBy,
            Long acceptedBy,
            Long completedBy,
            Long saleId
    ) {
        return new OwnershipTransfer(id, goatId, sourceFarmId, targetFarmId, kind, status,
                reason, idempotencyKey, requestedAt, acceptedAt, effectiveAt, completedAt,
                cancelledAt, requestedBy, acceptedBy, completedBy, saleId);
    }

    /**
     * Normal acceptance atomically records acceptance and completion. The
     * application service must close/open ownership periods in the same
     * transaction before exposing the completed result.
     */
    public void acceptAndComplete(Instant acceptedAt, Instant completedAt, long acceptedBy, Instant effectiveAt) {
        requireStatus(OwnershipTransferStatus.REQUESTED);
        Objects.requireNonNull(acceptedAt, "acceptedAt must not be null");
        Objects.requireNonNull(completedAt, "completedAt must not be null");
        Objects.requireNonNull(effectiveAt, "effectiveAt must not be null");
        if (acceptedBy <= 0) {
            throw new IllegalArgumentException("acceptedBy must be positive");
        }
        if (completedAt.isBefore(acceptedAt)) {
            throw new IllegalArgumentException("completedAt must not precede acceptedAt");
        }
        if (acceptedAt.isBefore(requestedAt)) {
            throw new IllegalArgumentException("acceptedAt must not precede requestedAt");
        }
        if (!effectiveAt.equals(completedAt)) {
            throw new IllegalArgumentException("effectiveAt must equal completedAt in V1");
        }
        this.acceptedAt = acceptedAt;
        this.acceptedBy = acceptedBy;
        this.effectiveAt = completedAt;
        this.completedAt = completedAt;
        this.completedBy = acceptedBy;
        this.status = OwnershipTransferStatus.ACCEPTED;
        this.status = OwnershipTransferStatus.COMPLETED;
    }

    /** Administrative recovery for a transfer already accepted by policy. */
    public void completeAfterAcceptance(Instant completedAt, long completedBy, Instant effectiveAt) {
        requireStatus(OwnershipTransferStatus.ACCEPTED);
        Objects.requireNonNull(completedAt, "completedAt must not be null");
        Objects.requireNonNull(effectiveAt, "effectiveAt must not be null");
        if (completedBy <= 0) {
            throw new IllegalArgumentException("completedBy must be positive");
        }
        if (acceptedAt == null || completedAt.isBefore(acceptedAt)) {
            throw new IllegalArgumentException("completedAt must not precede acceptedAt");
        }
        if (!effectiveAt.equals(completedAt)) {
            throw new IllegalArgumentException("effectiveAt must equal completedAt in V1");
        }
        this.completedAt = completedAt;
        this.completedBy = completedBy;
        this.effectiveAt = completedAt;
        this.status = OwnershipTransferStatus.COMPLETED;
    }

    public void markAccepted(Instant acceptedAt, long acceptedBy) {
        requireStatus(OwnershipTransferStatus.REQUESTED);
        Objects.requireNonNull(acceptedAt, "acceptedAt must not be null");
        if (acceptedAt.isBefore(requestedAt)) {
            throw new IllegalArgumentException("acceptedAt must not precede requestedAt");
        }
        if (acceptedBy <= 0) {
            throw new IllegalArgumentException("acceptedBy must be positive");
        }
        this.acceptedAt = acceptedAt;
        this.acceptedBy = acceptedBy;
        this.status = OwnershipTransferStatus.ACCEPTED;
    }

    public void reject() {
        requireStatus(OwnershipTransferStatus.REQUESTED);
        this.status = OwnershipTransferStatus.REJECTED;
    }

    public void cancel(Instant cancelledAt) {
        if (status != OwnershipTransferStatus.REQUESTED && status != OwnershipTransferStatus.ACCEPTED) {
            throw new IllegalStateException("only a pending transfer can be cancelled");
        }
        this.cancelledAt = Objects.requireNonNull(cancelledAt, "cancelledAt must not be null");
        if (cancelledAt.isBefore(requestedAt)
                || (acceptedAt != null && cancelledAt.isBefore(acceptedAt))) {
            throw new IllegalArgumentException("cancelledAt must not precede transfer timestamps");
        }
        this.status = OwnershipTransferStatus.CANCELLED;
    }

    public boolean isTerminal() {
        return status == OwnershipTransferStatus.COMPLETED
                || status == OwnershipTransferStatus.REJECTED
                || status == OwnershipTransferStatus.CANCELLED;
    }

    public Long id() { return id; }
    public GoatId goatId() { return goatId; }
    public Long sourceFarmId() { return sourceFarmId; }
    public long targetFarmId() { return targetFarmId; }
    public OwnershipTransferKind kind() { return kind; }
    public OwnershipTransferStatus status() { return status; }
    public String reason() { return reason; }
    public String idempotencyKey() { return idempotencyKey; }
    public Instant requestedAt() { return requestedAt; }
    public Instant acceptedAt() { return acceptedAt; }
    public Instant effectiveAt() { return effectiveAt; }
    public Instant completedAt() { return completedAt; }
    public Instant cancelledAt() { return cancelledAt; }
    public long requestedBy() { return requestedBy; }
    public Long acceptedBy() { return acceptedBy; }
    public Long completedBy() { return completedBy; }
    public Long saleId() { return saleId; }

    private void requireStatus(OwnershipTransferStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("transfer must be in " + expected + " state");
        }
    }

    private void validateTimestamps() {
        if (acceptedAt != null && acceptedAt.isBefore(requestedAt)) {
            throw new IllegalArgumentException("acceptedAt must not precede requestedAt");
        }
        if (completedAt != null && acceptedAt != null && completedAt.isBefore(acceptedAt)) {
            throw new IllegalArgumentException("completedAt must not precede acceptedAt");
        }
        if (cancelledAt != null && (cancelledAt.isBefore(requestedAt)
                || (acceptedAt != null && cancelledAt.isBefore(acceptedAt)))) {
            throw new IllegalArgumentException("cancelledAt must not precede transfer timestamps");
        }
        if (effectiveAt != null && completedAt != null && !effectiveAt.equals(completedAt)) {
            throw new IllegalArgumentException("effectiveAt must equal completedAt in V1");
        }
        switch (status) {
            case REQUESTED -> {
                if (acceptedAt != null || effectiveAt != null || completedAt != null || cancelledAt != null
                        || acceptedBy != null || completedBy != null) {
                    throw new IllegalArgumentException("requested transfer has inconsistent lifecycle fields");
                }
            }
            case ACCEPTED -> {
                if (acceptedAt == null || acceptedBy == null || effectiveAt != null || completedAt != null
                        || cancelledAt != null || completedBy != null) {
                    throw new IllegalArgumentException("accepted transfer has inconsistent lifecycle fields");
                }
            }
            case COMPLETED -> {
                if (acceptedAt == null || acceptedBy == null || effectiveAt == null || completedAt == null
                        || completedBy == null || cancelledAt != null || !effectiveAt.equals(completedAt)) {
                    throw new IllegalArgumentException("completed transfer has inconsistent lifecycle fields");
                }
            }
            case REJECTED -> {
                if (acceptedAt != null || effectiveAt != null || completedAt != null || cancelledAt != null
                        || acceptedBy != null || completedBy != null) {
                    throw new IllegalArgumentException("rejected transfer has inconsistent lifecycle fields");
                }
            }
            case CANCELLED -> {
                if (cancelledAt == null || effectiveAt != null || completedAt != null || completedBy != null
                        || (acceptedAt == null) != (acceptedBy == null)) {
                    throw new IllegalArgumentException("cancelled transfer has inconsistent lifecycle fields");
                }
            }
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
