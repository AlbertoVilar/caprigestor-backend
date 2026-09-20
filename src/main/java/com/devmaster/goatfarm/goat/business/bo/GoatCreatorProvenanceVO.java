package com.devmaster.goatfarm.goat.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Explicit creator evidence supplied by a creation flow. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatCreatorProvenanceVO {
    private Long creatorFarmId;
    private String creatorTod;
    private String creatorNameSnapshot;
    private String evidenceReference;
}
