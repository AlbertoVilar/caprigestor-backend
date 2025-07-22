package com.devmaster.goatfarm.farm.dao;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AddressRepository;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converters.GoatFarmConverter;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.dao.OwnerDAO;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import com.devmaster.goatfarm.owner.model.repository.OwnerRepository;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.dao.PhoneDAO;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.phone.model.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoatFarmDAO {

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private OwnerDAO ownerDAO;

    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private PhoneDAO phoneDAO;

    @Transactional
    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados da fazenda para criação não podem ser nulos.");
        }

        if (requestVO.getPhoneIds() == null || requestVO.getPhoneIds().isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone para a fazenda.");
        }

        if (goatFarmRepository.existsByName(requestVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + requestVO.getName() + "'.");
        }

        if (requestVO.getTod() != null && goatFarmRepository.existsByTod(requestVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + requestVO.getTod() + "'.");
        }

        Owner owner = ownerRepository.findById(requestVO.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dono não encontrado com o ID: " + requestVO.getOwnerId()));

        Address address = addressRepository.findById(requestVO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado com o ID: " + requestVO.getAddressId()));

        List<Phone> phones = phoneRepository.findAllById(requestVO.getPhoneIds());
        if (phones.size() != requestVO.getPhoneIds().size()) {
            throw new ResourceNotFoundException("Um ou mais telefones informados não foram encontrados.");
        }

        for (Phone phone : phones) {
            if (phone.getGoatFarm() != null) {
                throw new DatabaseException("Telefone DDD (" + phone.getDdd() + ") número " + phone.getNumber()
                        + " já está associado à fazenda: " + phone.getGoatFarm().getName());
            }
        }

        GoatFarm goatFarm = GoatFarmConverter.toEntity(requestVO, owner, address);
        goatFarm.setPhones(phones);

        try {
            goatFarm = goatFarmRepository.save(goatFarm);
            return GoatFarmConverter.toVO(goatFarm);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Ocorreu um erro ao salvar a fazenda: " + e.getMessage());
        }
    }

    @Transactional
    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO farmVO,
                                                 OwnerRequestVO ownerVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {

        // Validação de existência da fazenda
        GoatFarm goatFarmToUpdate = goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada."));

        // Validação de nome duplicado
        if (!goatFarmToUpdate.getName().equals(farmVO.getName()) &&
                goatFarmRepository.existsByName(farmVO.getName())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o nome '" + farmVO.getName() + "'.");
        }

        // Validação de TOD duplicado
        if (farmVO.getTod() != null &&
                (goatFarmToUpdate.getTod() == null || !goatFarmToUpdate.getTod().equals(farmVO.getTod())) &&
                goatFarmRepository.existsByTod(farmVO.getTod())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o código '" + farmVO.getTod() + "'.");
        }

        // Atualiza entidades relacionadas
        ownerDAO.updateGoatOwner(farmVO.getOwnerId(), ownerVO);
        addressDAO.updateAddress(farmVO.getAddressId(), addressVO);

        // Validação de telefones
        if (phoneVOs == null || phoneVOs.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone.");
        }

        // Atualiza os telefones individualmente
        for (PhoneRequestVO phoneVO : phoneVOs) {
            phoneDAO.updatePhone(phoneVO.getId(), phoneVO);
        }

        // Busca entidades Phone e atualiza na fazenda
        List<Long> phoneIds = phoneVOs.stream().map(PhoneRequestVO::getId).toList();
        List<Phone> phones = phoneRepository.findAllById(phoneIds);

        if (phones.size() != phoneIds.size()) {
            throw new ResourceNotFoundException("Um ou mais telefones informados não foram encontrados.");
        }

        // ⚠️ Correção do problema de orphanRemoval
        goatFarmToUpdate.getPhones().clear();
        goatFarmToUpdate.getPhones().addAll(phones);

        // Atualiza os dados da fazenda em si
        GoatFarmConverter.entityUpdate(goatFarmToUpdate, farmVO);

        try {
            GoatFarm updatedFarm = goatFarmRepository.save(goatFarmToUpdate);

            // ✅ Retorna dados completos
            return GoatFarmConverter.toFullVO(updatedFarm);

        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar a fazenda com ID " + id + ": " + e.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        GoatFarm goatFarm = goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada."));
        return GoatFarmConverter.toFullVO(goatFarm);
    }

    @Transactional(readOnly = true)
    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        Page<GoatFarm> resultGoatFarms = goatFarmRepository.searchGoatFarmByName(name, pageable);
        return resultGoatFarms.map(GoatFarmConverter::toFullVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        Page<GoatFarm> projections = goatFarmRepository.searchAllFullFarms(pageable);
        return projections.map(GoatFarmConverter::toFullVO);
    }

    @Transactional
    public String deleteGoatFarm(Long id) {
        if (!goatFarmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada.");
        }
        try {
            goatFarmRepository.deleteById(id);
            return "Fazenda com ID " + id + " foi deletada com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar a fazenda com ID " + id +
                    " porque ela possui relacionamentos com outras entidades.");
        }
    }
}
