package com.devmaster.goatfarm.farm.application.ports.out;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.persistence.entity.User;

import java.util.Optional;

/** Transitional farm-owned reference to the authority aggregate used by the farm relation. */
public interface FarmUserPersistencePort {
    Optional<User> findByEmail(String email);

    User findOrCreate(UserRequestVO request);
}
