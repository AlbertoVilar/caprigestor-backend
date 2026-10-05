package com.devmaster.goatfarm.farm.application.ports.in;

/** Public Farm capability used by other bounded contexts to validate scope. */
public interface FarmExistenceQueryUseCase {
    boolean existsById(Long farmId);
}
