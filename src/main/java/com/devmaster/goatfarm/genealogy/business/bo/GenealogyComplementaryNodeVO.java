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
    private String localGoatId;
}

