package com.devmaster.goatfarm.farm.application.ports.in;

import com.devmaster.goatfarm.farm.application.model.FarmRegistrationSnapshot;

import java.util.Optional;

public interface FarmRegistrationQueryUseCase {
    Optional<FarmRegistrationSnapshot> findRegistrationById(Long farmId);
}
