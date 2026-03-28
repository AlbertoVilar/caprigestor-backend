package com.devmaster.goatfarm.inventory.persistence.entity;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory_movement")
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private InventoryMovementType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjust_direction", length = 20)
    private InventoryAdjustDirection adjustDirection;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "resulting_balance", nullable = false, precision = 19, scale = 3)
    private BigDecimal resultingBalance;

    @Column(name = "unit_cost", precision = 14, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 14, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "supplier_name", length = 120)
    private String supplierName;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
