package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.enums.Category;

/** Resolves local and external parent references without exposing JPA. */
public interface GoatParentagePort {

    ResolvedParentage resolve(Category category, String childRegistrationNumber,
                               String fatherRegistrationNumber, String motherRegistrationNumber);

    record ResolvedParentage(Goat.ParentReference father, Goat.ParentReference mother) {

        public String externalFatherRegistrationNumber() {
            return externalRegistration(father);
        }

        public String externalMotherRegistrationNumber() {
            return externalRegistration(mother);
        }

        private static String externalRegistration(Goat.ParentReference reference) {
            return reference != null && !reference.isLocal() ? reference.registrationNumber() : null;
        }
    }
}
