package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.sharedkernel.pregnancy.PregnancySnapshot;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Public read capability for pregnancy information exposed by Reproduction.
 *
 * <p>The contract deliberately returns the framework-free shared snapshot;
 * Reproduction persistence entities and repositories remain internal to the
 * module.</p>
 */
public interface PregnancySnapshotQueryUseCase {

    /**
     * Returns the latest pregnancy snapshot for a farm/goat as of the
     * reference date.
     */
    Optional<PregnancySnapshot> findLatestByFarmIdAndGoatId(
            Long farmId,
            String goatId,
            LocalDate referenceDate
    );
}
