package com.devmaster.goatfarm.commercial.persistence.adapter;

import com.devmaster.goatfarm.commercial.application.model.AnimalSaleCommand;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleRecord;
import com.devmaster.goatfarm.commercial.application.model.CustomerRecord;
import com.devmaster.goatfarm.commercial.application.model.CustomerReference;
import com.devmaster.goatfarm.commercial.application.model.MilkSaleCommand;
import com.devmaster.goatfarm.commercial.application.model.MilkSaleRecord;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSalePersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.CustomerPersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.MilkSalePersistencePort;
import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import com.devmaster.goatfarm.commercial.persistence.entity.MilkSale;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.CustomerRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.MilkSaleRepository;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommercialPersistenceAdapter implements CustomerPersistencePort, AnimalSalePersistencePort, MilkSalePersistencePort {

    private final CustomerRepository customerRepository;
    private final AnimalSaleRepository animalSaleRepository;
    private final MilkSaleRepository milkSaleRepository;
    private final GoatReferenceQueryPort goatReferenceQueryPort;
    private final GoatFarmRepository goatFarmRepository;

    public CommercialPersistenceAdapter(CustomerRepository customerRepository,
                                        AnimalSaleRepository animalSaleRepository,
                                        MilkSaleRepository milkSaleRepository,
                                        GoatReferenceQueryPort goatReferenceQueryPort,
                                        GoatFarmRepository goatFarmRepository) {
        this.customerRepository = customerRepository;
        this.animalSaleRepository = animalSaleRepository;
        this.milkSaleRepository = milkSaleRepository;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.goatFarmRepository = goatFarmRepository;
    }

    @Override
    public CustomerRecord save(CustomerRecord record) {
        Customer entity = record.id() == null ? new Customer() : customerRepository.findById(record.id()).orElseGet(Customer::new);
        entity.setFarm(goatFarmRepository.getReferenceById(record.farmId()));
        entity.setName(record.name());
        entity.setDocument(record.document());
        entity.setPhone(record.phone());
        entity.setEmail(record.email());
        entity.setNotes(record.notes());
        entity.setActive(record.active());
        return toRecord(customerRepository.save(entity));
    }

    @Override
    public List<CustomerRecord> findCustomersByFarmId(Long farmId) {
        return customerRepository.findByFarm_IdOrderByNameAsc(farmId).stream().map(this::toRecord).toList();
    }

    @Override
    public Optional<CustomerRecord> findCustomerByIdAndFarmId(Long customerId, Long farmId) {
        return customerRepository.findByIdAndFarm_Id(customerId, farmId).map(this::toRecord);
    }

    @Override
    public long countCustomersByFarmId(Long farmId) {
        return customerRepository.countByFarm_Id(farmId);
    }

    @Override
    public AnimalSaleRecord save(AnimalSaleCommand command) {
        AnimalSale entity = command.id() == null ? new AnimalSale() : animalSaleRepository.findById(command.id()).orElseGet(AnimalSale::new);
        entity.setFarm(goatFarmRepository.getReferenceById(command.farmId()));
        entity.setCustomer(customerRepository.findByIdAndFarm_Id(command.customerId(), command.farmId()).orElseThrow());
        entity.setGoatTechnicalId(command.goatTechnicalId());
        entity.setGoatRegistrationNumber(command.goatRegistrationNumber());
        entity.setGoatName(command.goatName());
        entity.setSaleDate(command.saleDate());
        entity.setAmount(command.amount());
        entity.setDueDate(command.dueDate());
        entity.setPaymentStatus(command.paymentStatus());
        entity.setPaymentDate(command.paymentDate());
        entity.setNotes(command.notes());
        // GoatId is the preferred structural reference. The RG lookup is a
        // compatibility fallback for legacy records that do not carry it.
        if (entity.getGoatTechnicalId() == null) {
            goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(command.goatRegistrationNumber(), command.farmId())
                    .map(GoatReference::id).map(id -> id.value()).ifPresent(entity::setGoatTechnicalId);
        }
        return toRecord(animalSaleRepository.save(entity));
    }

    @Override
    public boolean existsByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId) {
        return goatTechnicalId != null && animalSaleRepository.existsByFarm_IdAndGoatTechnicalId(farmId, goatTechnicalId);
    }

    @Override
    public boolean existsByLegacyRegistrationNumber(String registrationNumber) {
        return animalSaleRepository.existsByGoatRegistrationNumber(registrationNumber);
    }

    @Override
    public Optional<AnimalSaleRecord> findAnimalSaleByIdAndFarmId(Long saleId, Long farmId) {
        return animalSaleRepository.findByIdAndFarm_Id(saleId, farmId).map(this::toRecord);
    }

    @Override
    public List<AnimalSaleRecord> findAnimalSalesByFarmId(Long farmId) {
        return animalSaleRepository.findByFarm_IdOrderBySaleDateDescIdDesc(farmId).stream().map(this::toRecord).toList();
    }

    @Override
    public MilkSaleRecord save(MilkSaleCommand command) {
        MilkSale entity = command.id() == null ? new MilkSale() : milkSaleRepository.findById(command.id()).orElseGet(MilkSale::new);
        entity.setFarm(goatFarmRepository.getReferenceById(command.farmId()));
        entity.setCustomer(customerRepository.findByIdAndFarm_Id(command.customerId(), command.farmId()).orElseThrow());
        entity.setSaleDate(command.saleDate());
        entity.setQuantityLiters(command.quantityLiters());
        entity.setUnitPrice(command.unitPrice());
        entity.setTotalAmount(command.totalAmount());
        entity.setDueDate(command.dueDate());
        entity.setPaymentStatus(command.paymentStatus());
        entity.setPaymentDate(command.paymentDate());
        entity.setNotes(command.notes());
        return toRecord(milkSaleRepository.save(entity));
    }

    @Override
    public Optional<MilkSaleRecord> findMilkSaleByIdAndFarmId(Long saleId, Long farmId) {
        return milkSaleRepository.findByIdAndFarm_Id(saleId, farmId).map(this::toRecord);
    }

    @Override
    public List<MilkSaleRecord> findMilkSalesByFarmId(Long farmId) {
        return milkSaleRepository.findByFarm_IdOrderBySaleDateDescIdDesc(farmId).stream().map(this::toRecord).toList();
    }

    private CustomerRecord toRecord(Customer entity) {
        return new CustomerRecord(entity.getId(), entity.getFarm().getId(), entity.getName(), entity.getDocument(), entity.getPhone(), entity.getEmail(), entity.getNotes(), entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private AnimalSaleRecord toRecord(AnimalSale entity) {
        Customer customer = entity.getCustomer();
        return new AnimalSaleRecord(entity.getId(), entity.getFarm().getId(), customer.getId(), new CustomerReference(customer.getId(), customer.getName(), customer.isActive()), entity.getGoatTechnicalId(), entity.getGoatRegistrationNumber(), entity.getGoatName(), entity.getSaleDate(), entity.getAmount(), entity.getDueDate(), entity.getPaymentStatus(), entity.getPaymentDate(), entity.getNotes(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private MilkSaleRecord toRecord(MilkSale entity) {
        Customer customer = entity.getCustomer();
        return new MilkSaleRecord(entity.getId(), entity.getFarm().getId(), customer.getId(), new CustomerReference(customer.getId(), customer.getName(), customer.isActive()), entity.getSaleDate(), entity.getQuantityLiters(), entity.getUnitPrice(), entity.getTotalAmount(), entity.getDueDate(), entity.getPaymentStatus(), entity.getPaymentDate(), entity.getNotes(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
