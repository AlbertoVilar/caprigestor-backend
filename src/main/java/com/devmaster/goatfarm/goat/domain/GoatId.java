package com.devmaster.goatfarm.goat.domain;

/**
 * Technical identity of a goat inside CapriGestor.
 *
 * <p>The value is deliberately independent from the registration number. The
 * latter is a business/external identifier and may be corrected in a future
 * domain operation without changing this identity.</p>
 */
public record GoatId(long value) {

    public GoatId {
        if (value <= 0) {
            throw new IllegalArgumentException("GoatId must be positive");
        }
    }

    public static GoatId of(Long value) {
        if (value == null) {
            return null;
        }
        return new GoatId(value);
    }
}
