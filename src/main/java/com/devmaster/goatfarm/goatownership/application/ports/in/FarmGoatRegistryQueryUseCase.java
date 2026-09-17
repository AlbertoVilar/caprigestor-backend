package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalDossierBasicItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;

import java.util.List;
import java.util.Optional;

public interface FarmGoatRegistryQueryUseCase {
    List<FarmGoatRegistryItem> findForFarm(long farmId);
    Optional<FarmGoatRegistryItem> findForFarmAndGoat(long farmId, GoatId goatId);
    Optional<FarmGoatHistoricalDossierBasicItem> findHistoricalDossierBasic(long farmId, GoatId goatId);
}
