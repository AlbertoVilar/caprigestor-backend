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
public class GenealogyComplementaryResponseVO {

    private GenealogyComplementaryNodeVO animalPrincipal;
    private GenealogyComplementaryNodeVO pai;
    private GenealogyComplementaryNodeVO mae;
    private GenealogyComplementaryNodeVO avoPaterno;
    private GenealogyComplementaryNodeVO avoPaterna;
    private GenealogyComplementaryNodeVO avoMaterno;
    private GenealogyComplementaryNodeVO avoMaterna;
    private GenealogyComplementaryNodeVO bisavoPaternoPai;
    private GenealogyComplementaryNodeVO bisavoPaternaPai;
    private GenealogyComplementaryNodeVO bisavoPaternoMae;
    private GenealogyComplementaryNodeVO bisavoPaternaMae;
    private GenealogyComplementaryNodeVO bisavoMaternoPai;
    private GenealogyComplementaryNodeVO bisavoMaternaPai;
    private GenealogyComplementaryNodeVO bisavoMaternoMae;
    private GenealogyComplementaryNodeVO bisavoMaternaMae;

    private GenealogyComplementaryIntegrationVO integration;
}

