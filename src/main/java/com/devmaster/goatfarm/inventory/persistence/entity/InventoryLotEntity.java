package com.devmaster.goatfarm.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;

@Entity
@Table(
        name = "inventory_lot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_lot_farm_item_code_normalized",
                        columnNames = {"farm_id", "item_id", "code_normalized"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class InventoryLotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "code", nullable = false, length = 80)
    private String code;

    @Column(name = "code_normalized", nullable = false, length = 100)
    private String codeNormalized;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @PrePersist
    @PreUpdate
    private void normalize() {
        this.code = normalizeText(this.code);
        this.codeNormalized = normalizeCode(this.code);
        this.description = normalizeNullableText(this.description);
    }

    private static String normalizeText(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    private static String normalizeNullableText(String input) {
        String normalized = normalizeText(input);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private static String normalizeCode(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = normalizeText(input);
        String noDiacritics = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noDiacritics.toLowerCase(Locale.ROOT);
    }
}
