package com.devmaster.goatfarm.goat.domain;

import java.util.Locale;

/** Business identity used by operators and at the ABCC boundary. */
public record RegistrationIdentity(String registrationNumber, String tod, String toe) {

    public RegistrationIdentity {
        registrationNumber = normalizeRequired(registrationNumber, "registrationNumber");
        tod = normalizeOptional(tod);
        toe = normalizeOptional(toe);
        if (tod != null && toe != null && !registrationNumber.equals(tod + toe)) {
            throw new IllegalArgumentException("registrationNumber must equal TOD + TOE when both are provided");
        }
    }

    public static RegistrationIdentity of(String registrationNumber, String tod, String toe) {
        return new RegistrationIdentity(registrationNumber, tod, toe);
    }

    /**
     * Creates a coherent registral identity from the two source ear markings.
     * The RG is deliberately derived server-side so clients cannot submit a
     * contradictory TOD/TOE/RG triple.
     */
    public static RegistrationIdentity fromTodAndToe(String tod, String toe) {
        String normalizedTod = normalizeRequired(tod, "tod");
        String normalizedToe = normalizeRequired(toe, "toe");
        return new RegistrationIdentity(normalizedTod + normalizedToe, normalizedTod, normalizedToe);
    }

    private static String normalizeRequired(String value, String field) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
