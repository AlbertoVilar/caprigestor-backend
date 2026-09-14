package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistory;

/** Application boundary for authorized reads of a goat's ownership history. */
public interface GoatOwnershipHistoryQueryUseCase {
    OwnershipHistory findOwnershipHistory(GoatId goatId);
}
