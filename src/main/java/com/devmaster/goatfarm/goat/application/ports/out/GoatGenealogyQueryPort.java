package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import java.util.Optional;

/**
 * Porta de saída (Output Port) para consultas de genealogia.
 * Segrega a responsabilidade de buscar o grafo de ancestrais.
 */
public interface GoatGenealogyQueryPort {

    /**
     * Busca uma cabra e carrega todo o grafo de ancestrais (pai/mãe, avós, bisavós)
     * e dados da fazenda/criador em uma única query otimizada.
     *
     * @param id ID da cabra (registration number)
     * @param farmId ID da fazenda
     * @return Optional com a cabra carregada
     */
    Optional<Goat> findByIdAndFarmIdWithFamilyGraph(String id, Long farmId);
}
