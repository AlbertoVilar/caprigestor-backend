package com.devmaster.goatfarm.genealogy.application.model;

public record GenealogyIntegrationSnapshot(
        String status,
        String lookupKey,
        String message
) {}
