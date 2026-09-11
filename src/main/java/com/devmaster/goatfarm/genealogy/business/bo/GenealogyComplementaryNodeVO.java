package com.devmaster.goatfarm.genealogy.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenealogyComplementaryNodeVO {

    private String relationship;
    private String name;
    private String registrationNumber;
    private GenealogyNodeSource source;

    /**
     * Legacy local identifier kept as the local RG for existing clients.
     * Prefer {@link #localTechnicalGoatId} for new internal navigation.
     */
    private String localGoatId;

    /** Immutable local GoatId; null for ABCC, declared and absent nodes. */
    private Long localTechnicalGoatId;
}

