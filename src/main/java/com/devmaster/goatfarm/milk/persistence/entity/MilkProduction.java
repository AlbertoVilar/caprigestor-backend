package com.devmaster.goatfarm.milk.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;

@Entity
@Table(name = "milk_production")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilkProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "goat_id", nullable = false, length = 50)
    private String goatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lactation_id", nullable = false)
    private Lactation lactation;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false)
    private MilkingShift shift;

    @Column(name = "volume_liters", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumeLiters;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MilkProductionStatus status = MilkProductionStatus.ACTIVE;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "canceled_reason", length = 500)
    private String canceledReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
