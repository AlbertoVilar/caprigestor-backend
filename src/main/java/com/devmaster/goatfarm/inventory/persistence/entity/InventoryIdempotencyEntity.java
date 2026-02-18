package com.devmaster.goatfarm.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "inventory_idempotency",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_idempotency_farm_key",
                        columnNames = {"farm_id", "idempotency_key"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class InventoryIdempotencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Lob
    @Column(name = "response_payload", nullable = false)
    private String responsePayload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
