package com.devmaster.goatfarm.farm.application.model;

/** Minimal farm projection exposed to the authenticated workspace selector. */
public record ManagedFarmRecord(Long id, String name, String tod, String logoUrl) { }
