package com.devmaster.goatfarm.events.model.entity;

import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.goat.model.entity.Goat;
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

    @ManyToOne
    @JoinColumn(name = "goat_registration_number", referencedColumnName = "num_registro", nullable = false)
    private Goat goat;

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

    public Goat getGoat() { return goat; }
    public void setGoat(Goat goat) { this.goat = goat; }
}
