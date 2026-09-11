package com.devmaster.goatfarm.goat.persistence.entity;

import com.devmaster.goatfarm.goat.enums.RegistrationRectificationSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** JPA representation of the immutable registration-correction history. */
@Entity
@Table(name = "goat_registration_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoatRegistrationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goat_id", nullable = false)
    private Long goatId;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "old_registration_number", nullable = false, length = 20)
    private String oldRegistrationNumber;

    @Column(name = "old_tod", length = 15)
    private String oldTod;

    @Column(name = "old_toe", length = 15)
    private String oldToe;

    @Column(name = "new_registration_number", nullable = false, length = 20)
    private String newRegistrationNumber;

    @Column(name = "new_tod", length = 15)
    private String newTod;

    @Column(name = "new_toe", length = 15)
    private String newToe;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private RegistrationRectificationSource source;

    @Column(name = "evidence_reference", nullable = false, length = 255)
    private String evidenceReference;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
