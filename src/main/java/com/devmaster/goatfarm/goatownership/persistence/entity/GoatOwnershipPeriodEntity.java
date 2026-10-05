package com.devmaster.goatfarm.goatownership.persistence.entity;

import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "goat_ownership_period")
@Getter
@Setter
@NoArgsConstructor
public class GoatOwnershipPeriodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goat_id", nullable = false, updatable = false)
    private Long goatId;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 32)
    private OwnershipEntryType entryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "exit_type", length = 32)
    private OwnershipExitType exitType;

    @Column(name = "source", nullable = false, length = 255)
    private String source;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
