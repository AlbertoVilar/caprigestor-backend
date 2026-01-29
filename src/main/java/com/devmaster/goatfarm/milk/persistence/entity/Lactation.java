package com.devmaster.goatfarm.milk.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.devmaster.goatfarm.milk.enums.LactationStatus;

@Entity
@Table(name = "lactation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lactation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "goat_id", nullable = false, length = 50)
    private String goatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LactationStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "pregnancy_start_date")
    private LocalDate pregnancyStartDate;

    @Column(name = "dry_start_date")
    private LocalDate dryStartDate;

    @Column(name = "dry_at_pregnancy_days")
    @Builder.Default
    private Integer dryAtPregnancyDays = 90;

    @Column(name = "rest_days")
    @Builder.Default
    private Integer restDays = 60;

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
