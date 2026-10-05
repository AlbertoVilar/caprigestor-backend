package com.devmaster.goatfarm.commercial.application.ports.out;

import com.devmaster.goatfarm.commercial.application.model.CustomerRecord;

import java.util.List;
import java.util.Optional;

public interface CustomerPersistencePort {
    CustomerRecord save(CustomerRecord customer);
    List<CustomerRecord> findCustomersByFarmId(Long farmId);
    Optional<CustomerRecord> findCustomerByIdAndFarmId(Long customerId, Long farmId);
    long countCustomersByFarmId(Long farmId);
}
