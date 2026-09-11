package com.devmaster.goatfarm.goat.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;

import java.time.LocalDate;

/**
 * Read model consumed by the genealogy bounded context.
 *
 * <p>Local ancestry is represented by a technical {@link GoatId}; registration
 * values remain business/display data. An external parent is intentionally an
 * RG-only reference because the ABCC boundary does not know local GoatIds.</p>
 */
public record GoatGenealogySnapshot(
        GoatId id,
        String registrationNumber,
        String name,
        GoatBreed breed,
        String color,
        GoatStatus status,
        Gender gender,
        Category category,
        String tod,
        String toe,
        LocalDate birthDate,
        String breederName,
        String farmOwnerName,
        ParentReference father,
        ParentReference mother
) {

    public record ParentReference(GoatGenealogySnapshot localGoat, String externalRegistrationNumber) {
        public static ParentReference local(GoatGenealogySnapshot goat) {
            return new ParentReference(goat, null);
        }

        public static ParentReference external(String registrationNumber) {
            return new ParentReference(null, registrationNumber);
        }

        public boolean isLocal() {
            return localGoat != null;
        }
    }
}
