package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goatownership.application.model.TerminalOwnershipExitCommand;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;

/** Inbound application boundary for terminal ownership exits. */
public interface GoatOwnershipExitUseCase {
    GoatOwnershipPeriod closeTerminalOwnership(TerminalOwnershipExitCommand command);
}
