package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.enums.Gender;

import java.util.Optional;

/**
 * Minimal outbound contract used by Goat parentage validation for an external
 * registration. The Goat context intentionally does not depend on the rich
 * Genealogy/ABCC projection.
 */
public interface GoatExternalParentQueryPort {

    Optional<ExternalParentReference> findByRegistrationNumber(String registrationNumber);

    record ExternalParentReference(String registrationNumber, Gender gender) {
    }
}
