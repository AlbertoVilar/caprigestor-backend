package com.devmaster.goatfarm.goatownership.persistence.entity;

import com.devmaster.goatfarm.goatownership.domain.CreatorSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "goat_creator_reference")
@Getter
@Setter
@NoArgsConstructor
public class CreatorReferenceEntity {
    @Id
    @Column(name = "goat_id", nullable = false, updatable = false)
    private Long goatId;

    @Column(name = "creator_tod", length = 5)
    private String creatorTod;

    @Column(name = "creator_farm_id")
    private Long creatorFarmId;

    @Column(name = "creator_name_snapshot", length = 255)
    private String creatorNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 32)
    private CreatorSource source;

    @Column(name = "evidence_reference", length = 500)
    private String evidenceReference;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}
