package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.enums.Category;

/** Resolves local and external parent references without exposing JPA. */
public interface GoatParentagePort {

    ResolvedParentage resolve(Category category, String childRegistrationNumber,
                               String fatherRegistrationNumber, String motherRegistrationNumber);

    record ResolvedParentage(Goat.ParentReference father, Goat.ParentReference mother) {
    }
}
