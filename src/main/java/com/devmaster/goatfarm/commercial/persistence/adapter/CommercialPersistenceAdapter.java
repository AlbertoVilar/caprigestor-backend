package com.devmaster.goatfarm.commercial.persistence.adapter;

import com.devmaster.goatfarm.commercial.application.ports.out.CommercialPersistencePort;
import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import com.devmaster.goatfarm.commercial.persistence.entity.MilkSale;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.CustomerRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.MilkSaleRepository;
import org.springframework.stereotype.Component;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;

import java.util.List;
import java.util.Optional;

@Component
public class CommercialPersistenceAdapter implements CommercialPersistencePort {

    private final CustomerRepository customerRepository;
    private final AnimalSaleRepository animalSaleRepository;
    private final MilkSaleRepository milkSaleRepository;
    private final GoatReferenceQueryPort goatReferenceQueryPort;
    private final GoatFarmRepository goatFarmRepository;

    public CommercialPersistenceAdapter(
            CustomerRepository customerRepository,
            AnimalSaleRepository animalSaleRepository,
            MilkSaleRepository milkSaleRepository,
            GoatReferenceQueryPort goatReferenceQueryPort,
            GoatFarmRepository goatFarmRepository
    ) {
        this.customerRepository = customerRepository;
        this.animalSaleRepository = animalSaleRepository;
        this.milkSaleRepository = milkSaleRepository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.goatFarmRepository = goatFarmRepository;
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        normalizeFarm(customer);
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
        normalizeFarm(animalSale);
        if (animalSale.getGoatTechnicalId() == null && animalSale.getFarm() != null) {
            goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(
                    animalSale.getGoatRegistrationNumber(), animalSale.getFarm().getId())
                    .map(GoatReference::id)
                    .map(id -> id.value())
                    .ifPresent(animalSale::setGoatTechnicalId);
        }
        return animalSaleRepository.save(animalSale);
    }

    @Override
    public boolean existsAnimalSaleByGoatRegistrationNumber(String goatRegistrationNumber) {
        return animalSaleRepository.existsByGoatRegistrationNumber(goatRegistrationNumber);
    }

    @Override
    public boolean existsAnimalSaleByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId) {
        return goatTechnicalId != null && animalSaleRepository.existsByFarm_IdAndGoatTechnicalId(farmId, goatTechnicalId);
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
        normalizeFarm(milkSale);
        return milkSaleRepository.save(milkSale);
    }

    private void normalizeFarm(Customer customer) {
        if (customer.getFarm() != null && customer.getFarm().getId() != null) {
            customer.setFarm(goatFarmRepository.getReferenceById(customer.getFarm().getId()));
        }
    }

    private void normalizeFarm(AnimalSale sale) {
        if (sale.getFarm() != null && sale.getFarm().getId() != null) {
            sale.setFarm(goatFarmRepository.getReferenceById(sale.getFarm().getId()));
        }
    }

    private void normalizeFarm(MilkSale sale) {
        if (sale.getFarm() != null && sale.getFarm().getId() != null) {
            sale.setFarm(goatFarmRepository.getReferenceById(sale.getFarm().getId()));
        }
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
