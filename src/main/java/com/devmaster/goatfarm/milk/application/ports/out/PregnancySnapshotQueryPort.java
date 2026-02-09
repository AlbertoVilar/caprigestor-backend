package com.devmaster.goatfarm.milk.application.ports.out;

import com.devmaster.goatfarm.sharedkernel.pregnancy.PregnancySnapshot;

import java.time.LocalDate;
import java.util.Optional;

public interface PregnancySnapshotQueryPort {

    /**
     * Returns the latest pregnancy snapshot for farm/goat.
     * Snapshot.active indicates whether pregnancy is active as-of referenceDate.
     */
    Optional<PregnancySnapshot> findLatestByFarmIdAndGoatId(Long farmId, String goatId, LocalDate referenceDate);
}
