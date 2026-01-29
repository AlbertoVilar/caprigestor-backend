package com.devmaster.goatfarm.health.persistence.entity;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "health_events", indexes = {
    @Index(name = "idx_health_farm_goat", columnList = "farm_id, goat_id"),
    @Index(name = "idx_health_farm_status", columnList = "farm_id, status"),
    @Index(name = "idx_health_farm_date", columnList = "farm_id, scheduled_date")
})
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

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(name = "responsible")
    private String responsible;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Campos de Medicação/Procedimento
    @Column(name = "product_name")
    private String productName;

    @Column(name = "active_ingredient")
    private String activeIngredient;

    @Column(name = "dose")
    private BigDecimal dose;

    @Enumerated(EnumType.STRING)
    @Column(name = "dose_unit")
    private DoseUnit doseUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "route")
    private AdministrationRoute route;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "withdrawal_milk_days")
    private Integer withdrawalMilkDays;

    @Column(name = "withdrawal_meat_days")
    private Integer withdrawalMeatDays;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
