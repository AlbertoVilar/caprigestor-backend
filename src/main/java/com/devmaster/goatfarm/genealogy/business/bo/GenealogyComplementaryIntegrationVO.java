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
public class GenealogyComplementaryIntegrationVO {

    private String status;
    private String lookupKey;
    private String message;
}

