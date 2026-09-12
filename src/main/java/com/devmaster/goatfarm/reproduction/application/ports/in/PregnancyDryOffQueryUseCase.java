package com.devmaster.goatfarm.reproduction.application.ports.in;

import com.devmaster.goatfarm.reproduction.application.model.PregnancyDryOffSnapshot;
import java.time.LocalDate;
import java.util.List;

/** Reproduction-owned batch read capability used by Milk dry-off alerts. */
public interface PregnancyDryOffQueryUseCase {
    List<PregnancyDryOffSnapshot> findLatestRelevantByFarmId(Long farmId, LocalDate referenceDate);
}
