package com.devmaster.goatfarm.health.persistence.entity;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_events")

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "goat_id", nullable = false)
    private String goatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HealthEventType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HealthEventStatus status;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(name = "responsible")
    private String responsible;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "dose", precision = 10, scale = 2)
    private BigDecimal dose;

    @Enumerated(EnumType.STRING)
    @Column(name = "dose_unit")
    private DoseUnit doseUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "route")
    private AdministrationRoute route;

    @Column(name = "withdrawal_milk_days")
    private Integer withdrawalMilkDays;

    @Column(name = "withdrawal_meat_days")
    private Integer withdrawalMeatDays;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
