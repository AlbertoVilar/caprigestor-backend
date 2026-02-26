package com.devmaster.goatfarm.inventory.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.text.Normalizer;
import java.util.Locale;

@Entity
@Table(name = "inventory_item")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "name_normalized", nullable = false, length = 140)
    private String nameNormalized;

    @Column(name = "track_lot", nullable = false)
    private boolean trackLot;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @PrePersist
    @PreUpdate
    private void normalize() {
        this.nameNormalized = normalizeName(this.name);
    }

    private static String normalizeName(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        String noDiacritics = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noDiacritics.toLowerCase(Locale.ROOT);
    }
}

