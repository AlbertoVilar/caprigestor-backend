package com.devmaster.goatfarm.goatownership.application.model;

/** Framework-neutral pagination request for ownership transfer queries. */
public record OwnershipTransferPageQuery(int page, int size) {
}
