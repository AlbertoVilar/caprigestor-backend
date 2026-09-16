package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import java.util.List;

public interface FarmGoatRegistryQueryPort {
    List<Candidate> findCandidates(long farmId);
    record Candidate(GoatId goatId, String registrationNumber, String name, GoatStatus globalStatus,
                     CreatorReference creatorReference, List<GoatOwnershipPeriod> ownershipHistory) { }
}
