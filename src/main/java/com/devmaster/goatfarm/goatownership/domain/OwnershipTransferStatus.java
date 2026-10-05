package com.devmaster.goatfarm.goatownership.domain;

/** Lifecycle state of an explicit ownership transfer request. */
public enum OwnershipTransferStatus {
    REQUESTED,
    ACCEPTED,
    COMPLETED,
    REJECTED,
    CANCELLED
}
