package com.devmaster.goatfarm.phone.persistence.adapter;

import com.devmaster.goatfarm.phone.application.ports.out.PhonePersistencePort;
import com.devmaster.goatfarm.phone.persistence.entity.Phone;
import com.devmaster.goatfarm.phone.persistence.repository.PhoneRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PhonePersistenceAdapter implements PhonePersistencePort {

    private final PhoneRepository phoneRepository;

    public PhonePersistenceAdapter(PhoneRepository phoneRepository) {
        this.phoneRepository = phoneRepository;
    }

    @Override
    public Phone save(Phone phone) {
        return phoneRepository.save(phone);
    }

    @Override
    public Optional<Phone> findByDddAndNumber(String ddd, String number) {
        return phoneRepository.findByDddAndNumber(ddd, number);
    }

    @Override
    public Optional<Phone> findByIdAndFarmId(Long id, Long farmId) {
        return phoneRepository.findByIdAndGoatFarmId(id, farmId);
    }

    @Override
    public List<Phone> findAllByFarmId(Long farmId) {
        return phoneRepository.findAllByGoatFarmId(farmId);
    }

    @Override
    public long countByFarmId(Long farmId) {
        return phoneRepository.countByGoatFarmId(farmId);
    }

    @Override
    public void deleteById(Long id) {
        phoneRepository.deleteById(id);
    }

    @Override
    public boolean existsByDddAndNumber(String ddd, String number) {
        return phoneRepository.existsByDddAndNumber(ddd, number);
    }

    @Override
    public List<Phone> findAllByIds(List<Long> ids) {
        return phoneRepository.findAllById(ids);
    }

    @Override
    public void deletePhonesFromOtherUsers(Long adminId) {
        phoneRepository.deletePhonesFromOtherUsers(adminId);
    }
}
