package com.devmaster.goatfarm.reproduction.model.entity;

import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pregnancy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pregnancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "goat_id", nullable = false, length = 50)
    private String goatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PregnancyStatus status;

    @Column(name = "breeding_date")
    private LocalDate breedingDate;

    @Column(name = "confirmed_at")
    private LocalDate confirmedAt;

    @Column(name = "expected_due_date")
    private LocalDate expectedDueDate;

    @Column(name = "recommended_dry_date")
    private LocalDate recommendedDryDate;

    @Column(name = "closed_at")
    private LocalDate closedAt;

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
