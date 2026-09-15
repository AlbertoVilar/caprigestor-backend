package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.goat.application.model.GoatCreationOrigin;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipInitializationCommand;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipInitializationUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/** Application service for the mandatory first ownership period of a Goat. */
@Service
public class GoatOwnershipInitializationBusiness implements GoatOwnershipInitializationUseCase {
    private final GoatOwnershipQueryPort ownershipQuery;
    private final GoatOwnershipPeriodPersistencePort periodPersistence;
    private final Clock clock;

    public GoatOwnershipInitializationBusiness(GoatOwnershipQueryPort ownershipQuery,
                                                GoatOwnershipPeriodPersistencePort periodPersistence,
                                                Clock clock) {
        this.ownershipQuery = ownershipQuery;
        this.periodPersistence = periodPersistence;
        this.clock = clock;
    }

    @Override
    @Transactional
    public GoatOwnershipPeriod initialize(GoatOwnershipInitializationCommand command) {
        validate(command);
        List<GoatOwnershipPeriod> history = ownershipQuery.findOwnershipHistory(command.goatId());
        if (history != null && !history.isEmpty()) {
            throw new BusinessRuleException("Goat already has canonical ownership history");
        }

        GoatOwnershipPeriod period = GoatOwnershipPeriod.open(
                command.goatId(),
                command.farmId(),
                Instant.now(clock),
                mapEntryType(command.origin()),
                sourceFor(command.origin()));
        GoatOwnershipPeriod.ensureConsistent(List.of(period));
        return periodPersistence.save(period);
    }

    private void validate(GoatOwnershipInitializationCommand command) {
        if (command == null || command.goatId() == null || command.goatId().value() <= 0) {
            throw new InvalidArgumentException("goatId", "GoatId must be a positive number");
        }
        if (command.farmId() <= 0) {
            throw new InvalidArgumentException("farmId", "farmId must be positive");
        }
        if (command.origin() == null) {
            throw new InvalidArgumentException("origin", "Goat creation origin is required");
        }
    }

    private OwnershipEntryType mapEntryType(GoatCreationOrigin origin) {
        return switch (origin) {
            case MANUAL -> OwnershipEntryType.MANUAL_IMPORT;
            case ABCC_IMPORT -> OwnershipEntryType.ABCC_IMPORT;
            case BIRTH -> OwnershipEntryType.BIRTH;
        };
    }

    private String sourceFor(GoatCreationOrigin origin) {
        return "GOAT_CREATE:" + origin.name();
    }
}
