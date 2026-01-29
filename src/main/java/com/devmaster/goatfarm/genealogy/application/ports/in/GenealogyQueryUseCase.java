package com.devmaster.goatfarm.genealogy.application.ports.in;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;

/**
 * Porta de entrada (Use Case) para consulta de genealogia.
 * Implementa o padrão de Query Use Case, retornando apenas projeções (Read Models).
 */
public interface GenealogyQueryUseCase {

    GenealogyResponseVO findGenealogy(Long farmId, String goatId);
}
