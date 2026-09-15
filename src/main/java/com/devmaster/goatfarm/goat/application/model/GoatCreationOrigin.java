package com.devmaster.goatfarm.goat.application.model;

/**
 * Neutral business provenance for a newly created Goat.
 *
 * <p>The origin is supplied by the application command path and is kept
 * independent from HTTP, JPA and ownership persistence details.</p>
 */
public enum GoatCreationOrigin {
    MANUAL,
    ABCC_IMPORT,
    BIRTH
}
