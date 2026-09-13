package com.devmaster.goatfarm.farm.application.model;

/** Scalar command consumed by the Farm persistence adapter. */
public record FarmPersistenceCommand(Long id, String name, String tod, String logoUrl,
                                     Long ownerUserId, Long addressId, Integer version) { }
