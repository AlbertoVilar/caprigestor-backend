package com.devmaster.goatfarm.farm.business.bo;

/** Minimal management choice; it deliberately omits owner/contact details. */
public record ManagedFarmSummaryVO(Long id, String name, String tod, String logoUrl) { }
