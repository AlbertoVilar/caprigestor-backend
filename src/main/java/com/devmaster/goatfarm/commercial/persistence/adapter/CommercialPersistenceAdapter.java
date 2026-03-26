package com.devmaster.goatfarm.commercial.persistence.adapter;

import com.devmaster.goatfarm.commercial.application.ports.out.CommercialPersistencePort;
import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import com.devmaster.goatfarm.commercial.persistence.entity.MilkSale;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.CustomerRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.MilkSaleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommercialPersistenceAdapter implements CommercialPersistencePort {

    private final CustomerRepository customerRepository;
    private final AnimalSaleRepository animalSaleRepository;
    private final MilkSaleRepository milkSaleRepository;

    public CommercialPersistenceAdapter(
            CustomerRepository customerRepository,
            AnimalSaleRepository animalSaleRepository,
            MilkSaleRepository milkSaleRepository
    ) {
        this.customerRepository = customerRepository;
        this.animalSaleRepository = animalSaleRepository;
        this.milkSaleRepository = milkSaleRepository;
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> findCustomersByFarmId(Long farmId) {
        return customerRepository.findByFarm_IdOrderByNameAsc(farmId);
    }

    @Override
    public Optional<Customer> findCustomerByIdAndFarmId(Long customerId, Long farmId) {
        return customerRepository.findByIdAndFarm_Id(customerId, farmId);
    }

    @Override
    public long countCustomersByFarmId(Long farmId) {
        return customerRepository.countByFarm_Id(farmId);
    }

    @Override
    public AnimalSale saveAnimalSale(AnimalSale animalSale) {
        return animalSaleRepository.save(animalSale);
    }

    @Override
    public boolean existsAnimalSaleByGoatRegistrationNumber(String goatRegistrationNumber) {
        return animalSaleRepository.existsByGoatRegistrationNumber(goatRegistrationNumber);
    }

    @Override
    public Optional<AnimalSale> findAnimalSaleByIdAndFarmId(Long saleId, Long farmId) {
        return animalSaleRepository.findByIdAndFarm_Id(saleId, farmId);
    }

    @Override
    public List<AnimalSale> findAnimalSalesByFarmId(Long farmId) {
        return animalSaleRepository.findByFarm_IdOrderBySaleDateDescIdDesc(farmId);
    }

    @Override
    public MilkSale saveMilkSale(MilkSale milkSale) {
        return milkSaleRepository.save(milkSale);
    }

    @Override
    public Optional<MilkSale> findMilkSaleByIdAndFarmId(Long saleId, Long farmId) {
        return milkSaleRepository.findByIdAndFarm_Id(saleId, farmId);
    }

    @Override
    public List<MilkSale> findMilkSalesByFarmId(Long farmId) {
        return milkSaleRepository.findByFarm_IdOrderBySaleDateDescIdDesc(farmId);
    }
}
