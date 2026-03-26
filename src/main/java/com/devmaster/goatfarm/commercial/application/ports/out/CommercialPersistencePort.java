package com.devmaster.goatfarm.commercial.application.ports.out;

import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import com.devmaster.goatfarm.commercial.persistence.entity.MilkSale;

import java.util.List;
import java.util.Optional;

public interface CommercialPersistencePort {

    Customer saveCustomer(Customer customer);

    List<Customer> findCustomersByFarmId(Long farmId);

    Optional<Customer> findCustomerByIdAndFarmId(Long customerId, Long farmId);

    long countCustomersByFarmId(Long farmId);

    AnimalSale saveAnimalSale(AnimalSale animalSale);

    boolean existsAnimalSaleByGoatRegistrationNumber(String goatRegistrationNumber);

    Optional<AnimalSale> findAnimalSaleByIdAndFarmId(Long saleId, Long farmId);

    List<AnimalSale> findAnimalSalesByFarmId(Long farmId);

    MilkSale saveMilkSale(MilkSale milkSale);

    Optional<MilkSale> findMilkSaleByIdAndFarmId(Long saleId, Long farmId);

    List<MilkSale> findMilkSalesByFarmId(Long farmId);
}
