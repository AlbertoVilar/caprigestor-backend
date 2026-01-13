package com.devmaster.goatfarm.reproduction.model.entity;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reproductive_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReproductiveEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "goat_id", nullable = false, length = 50)
    private String goatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private ReproductiveEventType eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "breeding_type")
    private BreedingType breedingType;

    @Column(name = "breeder_ref")
    private String breederRef;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "pregnancy_id")
    private Long pregnancyId;

    @Column(name = "check_scheduled_date")
    private LocalDate checkScheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_result")
    private PregnancyCheckResult checkResult;

    // Removed checkDate (redundant with eventDate)

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
