package com.devmaster.goatfarm.commercial.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "animal_sale_reversal", uniqueConstraints = @UniqueConstraint(name = "uk_animal_sale_reversal_sale", columnNames = "sale_id"))
@Getter
@Setter
@NoArgsConstructor
public class AnimalSaleReversal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private AnimalSale sale;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "reversed_at", nullable = false)
    private LocalDateTime reversedAt;

    @Column(name = "reversed_by")
    private Long reversedBy;
}
