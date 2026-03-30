package com.devmaster.goatfarm.milk.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "farm_milk_production",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_farm_milk_production_farm_date",
                        columnNames = {"farm_id", "production_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmMilkProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "total_produced", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProduced;

    @Column(name = "withdrawal_produced", nullable = false, precision = 12, scale = 2)
    private BigDecimal withdrawalProduced;

    @Column(name = "marketable_produced", nullable = false, precision = 12, scale = 2)
    private BigDecimal marketableProduced;

    @Column(name = "notes", length = 1000)
    private String notes;

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
