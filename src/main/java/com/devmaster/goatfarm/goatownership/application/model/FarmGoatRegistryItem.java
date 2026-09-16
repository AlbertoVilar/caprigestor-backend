package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import java.util.Set;

public record FarmGoatRegistryItem(GoatId goatId, String registrationNumber, String name,
                                   GoatStatus globalStatus, Long creatorFarmId,
                                   String creatorNameSnapshot, Set<FarmGoatRegistryRole> roles,
                                   FarmGoatRegistryDisposition disposition, Long currentOwnerFarmId) { }
