package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;

import java.util.Optional;

/** Persistence contract for the immutable creator provenance of a Goat. */
public interface CreatorReferencePersistencePort {
    CreatorReference create(GoatId goatId, CreatorReference creatorReference);

    Optional<CreatorReference> findByGoatId(GoatId goatId);
}
