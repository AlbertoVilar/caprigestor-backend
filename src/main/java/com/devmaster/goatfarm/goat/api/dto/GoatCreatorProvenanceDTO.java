package com.devmaster.goatfarm.goat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Explicit creator evidence for manual creation; never inferred from ownership. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatCreatorProvenanceDTO {
    private Long creatorFarmId;
    private String creatorTod;
    private String creatorNameSnapshot;
    private String evidenceReference;
}
