package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatRegistryQueryPort;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class FarmGoatRegistryQueryBusiness implements FarmGoatRegistryQueryUseCase {

    private final FarmGoatRegistryQueryPort queryPort;

    public FarmGoatRegistryQueryBusiness(FarmGoatRegistryQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public List<FarmGoatRegistryItem> findForFarm(long farmId) {
        if (farmId <= 0) {
            throw new IllegalArgumentException("farmId must be positive");
        }
        return queryPort.findCandidates(farmId).stream()
                .sorted(Comparator.comparingLong(candidate -> candidate.goatId().value()))
                .map(candidate -> toRegistryItem(candidate, farmId))
                .toList();
    }

    private FarmGoatRegistryItem toRegistryItem(FarmGoatRegistryQueryPort.Candidate candidate, long farmId) {
        List<GoatOwnershipPeriod> history = candidate.ownershipHistory();
        GoatOwnershipPeriod.ensureConsistent(history);

        Set<FarmGoatRegistryRole> roles = EnumSet.noneOf(FarmGoatRegistryRole.class);
        CreatorReference creator = candidate.creatorReference();
        if (creator != null && Objects.equals(creator.creatorFarmId(), farmId)) {
            roles.add(FarmGoatRegistryRole.CREATOR);
        }

        GoatOwnershipPeriod current = history.stream()
                .filter(GoatOwnershipPeriod::isOpen)
                .findFirst()
                .orElse(null);

        if (current != null && current.farmId() == farmId) {
            roles.add(FarmGoatRegistryRole.CURRENT_OWNER);
        } else if (history.stream().anyMatch(p -> p.farmId() == farmId)) {
            roles.add(FarmGoatRegistryRole.FORMER_OWNER);
        }

        return new FarmGoatRegistryItem(
                candidate.goatId(),
                candidate.registrationNumber(),
                candidate.name(),
                candidate.globalStatus(),
                creator == null ? null : creator.creatorFarmId(),
                creator == null ? null : creator.creatorNameSnapshot(),
                Set.copyOf(roles),
                resolveDisposition(history, farmId, current),
                current == null ? null : current.farmId()
        );
    }

    private FarmGoatRegistryDisposition resolveDisposition(
            List<GoatOwnershipPeriod> periods,
            long farmId,
            GoatOwnershipPeriod current
    ) {
        if (current != null && current.farmId() == farmId) {
            return FarmGoatRegistryDisposition.CURRENT;
        }

        return periods.stream()
                .filter(p -> p.farmId() == farmId && !p.isOpen())
                .max(Comparator.comparing(GoatOwnershipPeriod::endedAt))
                .map(p -> switch (p.exitType()) {
                    case EXTERNAL_SALE -> FarmGoatRegistryDisposition.SOLD;
                    case TRANSFER_OUT -> FarmGoatRegistryDisposition.TRANSFERRED;
                    case DONATION -> FarmGoatRegistryDisposition.DONATED;
                    case RETIREMENT -> FarmGoatRegistryDisposition.RETIRED;
                    case DEATH -> FarmGoatRegistryDisposition.DECEASED;
                })
                .orElse(FarmGoatRegistryDisposition.NONE);
    }
}
