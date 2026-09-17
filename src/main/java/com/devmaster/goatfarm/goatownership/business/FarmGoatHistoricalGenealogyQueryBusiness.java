package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyTreeProjectionUseCase;
import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;
import com.devmaster.goatfarm.goat.application.ports.in.GoatGenealogyReadUseCase;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalGenealogyQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FarmGoatHistoricalGenealogyQueryBusiness implements FarmGoatHistoricalGenealogyQueryUseCase {

    private final FarmGoatRegistryQueryUseCase registryQueryUseCase;
    private final GoatGenealogyReadUseCase goatGenealogyReadUseCase;
    private final GenealogyTreeProjectionUseCase genealogyTreeProjectionUseCase;

    public FarmGoatHistoricalGenealogyQueryBusiness(
            FarmGoatRegistryQueryUseCase registryQueryUseCase,
            GoatGenealogyReadUseCase goatGenealogyReadUseCase,
            GenealogyTreeProjectionUseCase genealogyTreeProjectionUseCase
    ) {
        this.registryQueryUseCase = Objects.requireNonNull(registryQueryUseCase, "registryQueryUseCase must not be null");
        this.goatGenealogyReadUseCase = Objects.requireNonNull(goatGenealogyReadUseCase, "goatGenealogyReadUseCase must not be null");
        this.genealogyTreeProjectionUseCase = Objects.requireNonNull(genealogyTreeProjectionUseCase, "genealogyTreeProjectionUseCase must not be null");
    }

    @Override
    public Optional<GenealogyTreeSnapshot> findHistoricalGenealogy(long farmId, GoatId goatId, boolean complementaryAbcc) {
        if (farmId <= 0) {
            throw new IllegalArgumentException("farmId must be positive");
        }
        if (goatId == null) {
            throw new IllegalArgumentException("goatId must not be null");
        }

        Optional<FarmGoatRegistryItem> registryItemOpt = registryQueryUseCase.findForFarmAndGoat(farmId, goatId);
        if (registryItemOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<GoatGenealogySnapshot> rootSnapshotOpt = goatGenealogyReadUseCase.findGenealogyByGoatId(goatId);
        if (rootSnapshotOpt.isEmpty()) {
            return Optional.empty();
        }

        GoatGenealogySnapshot root = rootSnapshotOpt.get();
        if (complementaryAbcc) {
            return Optional.of(genealogyTreeProjectionUseCase.complementWithAbcc(root));
        } else {
            return Optional.of(genealogyTreeProjectionUseCase.projectLocal(root));
        }
    }
}
