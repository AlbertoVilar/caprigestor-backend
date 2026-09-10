package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import java.util.Optional;

/**
 * Transitional legacy port for the genealogy graph.
 *
 * <p>This port intentionally exposes the JPA graph only to the legacy
 * genealogy adapter. New Goat application code must use the domain-facing
 * {@code GoatPersistencePort} instead.</p>
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
    Optional<GoatEntity> findByIdAndFarmIdWithFamilyGraph(String id, Long farmId);
}
