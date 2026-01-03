package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;

/**
 * Porta de entrada (Use Case) para operações legadas de genealogia.
 * Mantida apenas para compatibilidade retroativa.
 * @deprecated Use GenealogyManagementUseCase para operações suportadas ou atualize a Cabra diretamente.
 */
@Deprecated
public interface LegacyGenealogyUseCase {

    @Deprecated
    GenealogyResponseVO createGenealogy(Long farmId, String goatId);

    @Deprecated
    GenealogyResponseVO createGenealogyWithData(Long farmId, String goatId, GenealogyRequestDTO requestDTO);
}
