package com.devmaster.goatfarm.goat.domain;

import java.util.Locale;

/**
 * Business identity used by operators and by the ABCC boundary.
 *
 * <p>No value is inferred from another one: TOD, TOE and the composed RG are
 * retained as provided after the canonical whitespace/case normalization.</p>
 */
public record RegistrationIdentity(String registrationNumber, String tod, String toe) {

    public RegistrationIdentity {
        registrationNumber = normalizeRequired(registrationNumber, "registrationNumber");
        tod = normalizeOptional(tod);
        toe = normalizeOptional(toe);
    }

    public static RegistrationIdentity of(String registrationNumber, String tod, String toe) {
        return new RegistrationIdentity(registrationNumber, tod, toe);
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
