package com.devmaster.goatfarm.phone.persistence.adapter;

import com.devmaster.goatfarm.phone.application.ports.out.PhonePersistencePort;
import com.devmaster.goatfarm.application.exception.PersistenceConflictException;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.persistence.entity.Phone;
import com.devmaster.goatfarm.phone.persistence.repository.PhoneRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@Component
public class PhonePersistenceAdapter implements PhonePersistencePort {

    private final PhoneRepository phoneRepository;
    private final GoatFarmRepository farmRepository;

    public PhonePersistenceAdapter(PhoneRepository phoneRepository, GoatFarmRepository farmRepository) {
        this.phoneRepository = phoneRepository;
        this.farmRepository = farmRepository;
    }

    @Override
    public PhoneResponseVO save(Long farmId, PhoneRequestVO data) {
        Phone entity = data.getId() == null ? new Phone() : phoneRepository.findByIdAndGoatFarmId(data.getId(), farmId).orElseGet(Phone::new);
        entity.setDdd(data.getDdd());
        entity.setNumber(data.getNumber());
        if (entity.getGoatFarm() == null || entity.getGoatFarm().getId() == null) {
            GoatFarm farm = farmRepository.findById(farmId).orElseThrow(() -> new IllegalArgumentException("Fazenda não encontrada: " + farmId));
            entity.setGoatFarm(farm);
        }
        Phone saved;
        try {
            saved = phoneRepository.save(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException("Conflito de persistência ao salvar o telefone.", exception);
        }
        return toResponse(saved);
    }

    @Override
    public Optional<PhoneResponseVO> findByDddAndNumber(String ddd, String number) {
        return phoneRepository.findByDddAndNumber(ddd, number).map(this::toResponse);
    }

    @Override
    public Optional<PhoneResponseVO> findByIdAndFarmId(Long id, Long farmId) {
        return phoneRepository.findByIdAndGoatFarmId(id, farmId).map(this::toResponse);
    }

    @Override
    public List<PhoneResponseVO> findAllByFarmId(Long farmId) {
        return phoneRepository.findAllByGoatFarmId(farmId).stream().map(this::toResponse).toList();
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
    public List<PhoneResponseVO> findAllByIds(List<Long> ids) {
        return phoneRepository.findAllById(ids).stream().map(this::toResponse).toList();
    }

    private PhoneResponseVO toResponse(Phone phone) {
        return new PhoneResponseVO(phone.getId(), phone.getDdd(), phone.getNumber());
    }

}
