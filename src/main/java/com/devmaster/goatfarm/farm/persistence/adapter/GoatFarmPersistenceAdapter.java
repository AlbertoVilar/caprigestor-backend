package com.devmaster.goatfarm.farm.persistence.adapter;

import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.address.persistence.repository.AddressRepository;
import com.devmaster.goatfarm.farm.application.model.FarmPersistenceCommand;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.model.OwnerReference;
import com.devmaster.goatfarm.farm.application.ports.in.FarmExistenceQueryUseCase;
import com.devmaster.goatfarm.farm.application.ports.in.FarmRegistrationQueryUseCase;
import com.devmaster.goatfarm.farm.application.ports.out.FarmOwnerQueryPort;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GoatFarmPersistenceAdapter implements GoatFarmPersistencePort,
        FarmOwnerQueryPort, FarmRegistrationQueryUseCase, FarmExistenceQueryUseCase {
    private final GoatFarmRepository repository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public GoatFarmPersistenceAdapter(GoatFarmRepository repository, UserRepository userRepository, AddressRepository addressRepository) { this.repository = repository; this.userRepository = userRepository; this.addressRepository = addressRepository; }

    @Override public Optional<FarmRecord> findById(Long id) { return repository.findById(id).map(this::toRecord); }
    @Override public Optional<FarmRecord> findByIdAndUserId(Long id, Long userId) { return repository.findByIdAndUserId(id, userId).map(this::toRecord); }
    @Override public Optional<FarmRecord> findByAddressId(Long addressId) { return repository.findByAddressId(addressId).map(this::toRecord); }
    @Override public Optional<FarmRecord> findByIdWithDetails(Long id) { return repository.findByIdWithDetails(id).map(this::toRecord); }
    @Override public Page<FarmRecord> searchByName(String name, Pageable pageable) { return repository.searchGoatFarmByName(name, pageable).map(this::toRecord); }
    @Override public Page<FarmRecord> findAll(Pageable pageable) { return repository.findAll(pageable).map(this::toRecord); }
    @Override public boolean existsByName(String name) { return repository.existsByName(name); }
    @Override public boolean existsByTod(String tod) { return repository.existsByTod(tod); }

    @Override
    public FarmRecord save(FarmPersistenceCommand command) {
        GoatFarm farm = command.id() == null ? new GoatFarm() : repository.findById(command.id()).orElseGet(GoatFarm::new);
        farm.setName(command.name()); farm.setTod(command.tod()); farm.setLogoUrl(command.logoUrl());
        if (command.ownerUserId() != null && (farm.getUser() == null || !command.ownerUserId().equals(farm.getUser().getId()))) {
            User owner = userRepository.findById(command.ownerUserId()).orElseThrow(() -> new IllegalArgumentException("Usuário proprietário não encontrado: " + command.ownerUserId())); farm.setUser(owner);
        }
        if (command.addressId() != null && (farm.getAddress() == null || !command.addressId().equals(farm.getAddress().getId()))) {
            com.devmaster.goatfarm.address.persistence.entity.Address address = addressRepository.findById(command.addressId()).orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado: " + command.addressId())); farm.setAddress(address);
        }
        return toRecord(repository.save(farm));
    }
    @Override public void deleteById(Long id) { repository.deleteById(id); }
    @Override public boolean existsById(Long farmId) { return repository.existsById(farmId); }
    @Override public Optional<com.devmaster.goatfarm.farm.application.model.FarmRegistrationSnapshot> findRegistrationById(Long farmId) { return repository.findById(farmId).map(f -> new com.devmaster.goatfarm.farm.application.model.FarmRegistrationSnapshot(f.getId(), f.getTod())); }
    @Override public Optional<Long> findOwnerId(Long farmId) { return repository.findOwnerIdById(farmId); }

    private FarmRecord toRecord(GoatFarm farm) {
        User user = farm.getUser();
        OwnerReference owner = user == null ? null : new OwnerReference(user.getId(), user.getName(), user.getEmail(), user.getCpf(),
                user.getRoles() == null ? List.of() : user.getRoles().stream().map(r -> r.getAuthority()).toList());
        var addressEntity = farm.getAddress();
        AddressResponseVO address = addressEntity == null ? null : new AddressResponseVO(addressEntity.getId(), addressEntity.getStreet(), addressEntity.getCity(), addressEntity.getNeighborhood(), addressEntity.getState(), addressEntity.getZipCode(), addressEntity.getCountry());
        List<PhoneResponseVO> phones = farm.getPhones() == null ? List.of() : farm.getPhones().stream().map(p -> new PhoneResponseVO(p.getId(), p.getDdd(), p.getNumber())).toList();
        return new FarmRecord(farm.getId(), farm.getName(), farm.getTod(), farm.getLogoUrl(), owner, address, phones, farm.getCreatedAt(), farm.getUpdatedAt(), farm.getVersion());
    }
}
