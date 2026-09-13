package com.devmaster.goatfarm.goatownership.persistence.entity;

import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ownership_transfer")
@Getter
@Setter
@NoArgsConstructor
public class OwnershipTransferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goat_id", nullable = false, updatable = false)
    private Long goatId;

    @Column(name = "source_farm_id")
    private Long sourceFarmId;

    @Column(name = "target_farm_id", nullable = false)
    private Long targetFarmId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 32)
    private OwnershipTransferKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 16)
    private OwnershipTransferStatus status;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "effective_at")
    private Instant effectiveAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "accepted_by")
    private Long acceptedBy;

    @Column(name = "completed_by")
    private Long completedBy;

    @Column(name = "sale_id")
    private Long saleId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
