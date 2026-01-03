package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;

/**
 * Porta de entrada (Use Case) para operações de genealogia.
 */
public interface GenealogyManagementUseCase {

    GenealogyResponseVO findGenealogy(Long farmId, String goatId);
}