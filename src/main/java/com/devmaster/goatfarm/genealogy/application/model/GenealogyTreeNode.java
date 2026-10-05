package com.devmaster.goatfarm.genealogy.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;

public record GenealogyTreeNode(
        String relationship,
        String name,
        String registrationNumber,
        GenealogyNodeSource source,
        GoatId localGoatId
) {}
