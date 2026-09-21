package com.devmaster.goatfarm.goatownership.domain;

/** Business reason for opening an ownership period. */
public enum OwnershipEntryType {
    BIRTH,
    MANUAL_IMPORT,
    ABCC_IMPORT,
    PURCHASE,
    TRANSFER_IN,
    RETURN,
    EXTERNAL_CLAIM,
    CORRECTION_REENTRY
}
