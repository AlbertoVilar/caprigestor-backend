package com.devmaster.goatfarm.goat.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatParentagePort;
import com.devmaster.goatfarm.goat.business.GenealogicalParentageService;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.enums.Category;
import org.springframework.stereotype.Component;

/** Transitional adapter that shields the Goat core from the legacy JPA parent resolver. */
@Component
public class GoatParentageAdapter implements GoatParentagePort {

    private final GenealogicalParentageService delegate;

    public GoatParentageAdapter(GenealogicalParentageService delegate) {
        this.delegate = delegate;
    }

    @Override
    public ResolvedParentage resolve(Category category, String childRegistrationNumber,
                                     String fatherRegistrationNumber, String motherRegistrationNumber) {
        GenealogicalParentageService.ResolvedParentage resolved = delegate.resolve(
                category, childRegistrationNumber, fatherRegistrationNumber, motherRegistrationNumber);
        return new ResolvedParentage(toReference(resolved.father(), resolved.externalFatherRegistrationNumber()),
                toReference(resolved.mother(), resolved.externalMotherRegistrationNumber()));
    }

    private Goat.ParentReference toReference(GoatEntity local, String externalRegistration) {
        if (local != null) {
            return Goat.ParentReference.local(
                    com.devmaster.goatfarm.goat.domain.GoatId.of(local.getTechnicalId()),
                    local.getRegistrationNumber(), local.getName());
        }
        return externalRegistration == null ? null : Goat.ParentReference.external(externalRegistration);
    }
}
