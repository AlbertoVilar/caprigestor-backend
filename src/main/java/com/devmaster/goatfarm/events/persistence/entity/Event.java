package com.devmaster.goatfarm.events.persistence.entity;

import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "eventos")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "goat_technical_id")
    private Long goatTechnicalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goat_technical_id", referencedColumnName = "id", insertable = false, updatable = false)
    private GoatEntity goat;

    /**
     * Historical/business snapshot kept during the RG-to-GoatId transition.
     * The structural relation is {@link #goat} through goat_technical_id.
     */
    @Column(name = "goat_registration_number", nullable = false, length = 20)
    private String goatRegistrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false)
    private EventType eventType;

    @Column(name = "data", nullable = false)
    private LocalDate date;

    @Column(name = "descricao", length = 500)
    private String description;

    @Column(name = "local")
    private String location;

    @Column(name = "veterinario")
    private String veterinarian;

    @Column(name = "resultado")
    private String outcome;

    public GoatEntity getGoat() { return goat; }

    /** Assigns both the technical relationship and its explicit RG snapshot. */
    public void setGoat(GoatEntity goat) {
        this.goat = goat;
        this.goatTechnicalId = goat == null ? null : goat.getTechnicalId();
        this.goatRegistrationNumber = goat == null ? null : goat.getRegistrationNumber();
    }

    public Long getGoatTechnicalId() { return goatTechnicalId; }
    public void setGoatTechnicalId(Long goatTechnicalId) { this.goatTechnicalId = goatTechnicalId; }

    public String getGoatRegistrationNumber() { return goatRegistrationNumber; }
    public void setGoatRegistrationNumber(String goatRegistrationNumber) { this.goatRegistrationNumber = goatRegistrationNumber; }
}
