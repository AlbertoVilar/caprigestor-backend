package com.devmaster.goatfarm.farm.application.ports.out;

import com.devmaster.goatfarm.farm.application.model.FarmRecord;

import java.util.Optional;

/** Narrow cross-module farm lookup; consumers receive no persistence entity. */
public interface FarmReferenceQueryPort {
    Optional<FarmRecord> findById(Long farmId);
}
