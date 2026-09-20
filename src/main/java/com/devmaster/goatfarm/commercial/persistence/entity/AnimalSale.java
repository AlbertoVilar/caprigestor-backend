package com.devmaster.goatfarm.commercial.persistence.entity;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "animal_sale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private GoatFarm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Optional only for legacy external sales. A W13 ownership sale names the
     * receiving farm explicitly; ownership itself remains in the ledger.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_farm_id")
    private GoatFarm targetFarm;

    @Column(name = "goat_registration_number", nullable = false, length = 20)
    private String goatRegistrationNumber;

    /** Stable structural identity; RG/name are retained as sale-time snapshots. */
    @Column(name = "goat_technical_id")
    private Long goatTechnicalId;

    @Column(name = "goat_name", nullable = false, length = 100)
    private String goatName;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 10)
    private SalePaymentStatus paymentStatus;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "notes", length = 500)
    private String notes;

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
