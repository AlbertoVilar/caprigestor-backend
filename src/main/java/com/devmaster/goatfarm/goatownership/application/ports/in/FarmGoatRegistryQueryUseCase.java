package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import java.util.List;

public interface FarmGoatRegistryQueryUseCase { List<FarmGoatRegistryItem> findForFarm(long farmId); }
