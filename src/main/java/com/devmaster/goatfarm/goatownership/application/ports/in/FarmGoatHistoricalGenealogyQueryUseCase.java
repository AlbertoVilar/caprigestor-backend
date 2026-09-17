package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.goat.domain.GoatId;

import java.util.Optional;

public interface FarmGoatHistoricalGenealogyQueryUseCase {
    Optional<GenealogyTreeSnapshot> findHistoricalGenealogy(
            long farmId,
            GoatId goatId,
            boolean complementaryAbcc
    );
}
