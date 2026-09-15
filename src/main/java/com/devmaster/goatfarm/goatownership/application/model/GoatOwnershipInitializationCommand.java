package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.application.model.GoatCreationOrigin;
import com.devmaster.goatfarm.goat.domain.GoatId;

/** Command for opening the first canonical ownership period of a new Goat. */
public record GoatOwnershipInitializationCommand(
        GoatId goatId,
        long farmId,
        GoatCreationOrigin origin
) { }
