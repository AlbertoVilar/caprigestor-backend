package com.devmaster.goatfarm.farm.application.ports.out;

import java.util.Optional;

/**
 * Minimal ownership query used by authorization. It does not expose a JPA
 * farm entity across the security boundary.
 */
public interface FarmOwnerQueryPort {

    Optional<Long> findOwnerId(Long farmId);
}
