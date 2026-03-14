package com.devmaster.goatfarm.genealogy.application.ports.in;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryResponseVO;

public interface GenealogyComplementaryQueryUseCase {

    GenealogyComplementaryResponseVO findComplementaryGenealogy(Long farmId, String goatId);
}

