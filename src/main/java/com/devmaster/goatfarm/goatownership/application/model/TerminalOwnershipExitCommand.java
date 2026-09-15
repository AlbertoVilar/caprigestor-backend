package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;

/**
 * Structural command for closing a canonical ownership period without a
 * destination farm. The expected farm is a concurrency/authorization guard,
 * not a source of ownership truth.
 */
public record TerminalOwnershipExitCommand(
        GoatId goatId,
        Long expectedSourceFarmId,
        OwnershipExitType exitType
) {
}
