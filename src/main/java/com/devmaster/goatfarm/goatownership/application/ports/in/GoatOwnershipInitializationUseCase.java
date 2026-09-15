package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipInitializationCommand;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;

/** Opens the first canonical ownership period for a newly persisted Goat. */
public interface GoatOwnershipInitializationUseCase {
    GoatOwnershipPeriod initialize(GoatOwnershipInitializationCommand command);
}
