package com.devmaster.goatfarm.goatownership.domain;

/** Intent of an ownership transfer. */
public enum OwnershipTransferKind {
    INTERNAL_TRANSFER,
    INTERNAL_SALE,
    RETURN,
    EXTERNAL_CLAIM
}
