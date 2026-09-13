package com.devmaster.goatfarm.goatownership.domain;

/** Business reason for closing an ownership period. */
public enum OwnershipExitType {
    TRANSFER_OUT,
    EXTERNAL_SALE,
    DONATION,
    DEATH,
    RETIREMENT
}
