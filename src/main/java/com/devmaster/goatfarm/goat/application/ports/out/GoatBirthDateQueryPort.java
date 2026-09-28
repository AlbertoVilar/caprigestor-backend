package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.time.LocalDate;
import java.util.Optional;

/** Canonical birth date lookup by stable goat identity for cross-module rules. */
public interface GoatBirthDateQueryPort {

    Optional<LocalDate> findBirthDate(GoatId goatId);
}
