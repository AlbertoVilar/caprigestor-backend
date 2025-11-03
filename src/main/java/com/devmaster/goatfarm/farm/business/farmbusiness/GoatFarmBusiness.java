package com.devmaster.goatfarm.farm.business.farmbusiness;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoatFarmBusiness {

    private final GoatFarmDAO goatFarmDAO;
    private final AddressBusiness addressBusiness;
    private final UserBusiness userBusiness;
    private final PhoneBusiness phoneBusiness;
    private final GoatFarmMapper goatFarmMapper;

    @Autowired
    public GoatFarmBusiness(
            GoatFarmDAO goatFarmDAO,
            AddressBusiness addressBusiness,
            UserBusiness userBusiness,
            PhoneBusiness phoneBusiness,
            GoatFarmMapper goatFarmMapper
    ) {
        this.goatFarmDAO = goatFarmDAO;
        this.addressBusiness = addressBusiness;
        this.userBusiness = userBusiness;
        this.phoneBusiness = phoneBusiness;
        this.goatFarmMapper = goatFarmMapper;
    }

        @Transactional
    public GoatFarmFullResponseVO createFullGoatFarm(GoatFarmRequestVO farmVO,
                                                     UserRequestVO userVO,
                                                     AddressRequestVO addressVO,
                                                     List<PhoneRequestVO> phoneVOs) {
        if (farmVO == null) {
            throw new IllegalArgumentException("Os dados da fazenda para criaÃ§Ã£o nÃ£o podem ser nulos.");
        }
        if (userVO == null) {
            throw new IllegalArgumentException("Dados do usuÃ¡rio sÃ£o obrigatÃ³rios.");
        }
        if (addressVO == null) {
            throw new IllegalArgumentException("Dados do endereÃ§o sÃ£o obrigatÃ³rios.");
        }
        if (phoneVOs == null || phoneVOs.isEmpty()) {
            throw new IllegalArgumentException("Ã‰ obrigatÃ³rio informar ao menos um telefone para a fazenda.");
        }

        if (goatFarmDAO.existsByName(farmVO.getName())) {
            throw new DuplicateEntityException("JÃ¡ existe uma fazenda com o nome '" + farmVO.getName() + "'.");
        }
        if (farmVO.getTod() != null && goatFarmDAO.existsByTod(farmVO.getTod())) {
            throw new DuplicateEntityException("JÃ¡ existe uma fazenda com o cÃ³digo '" + farmVO.getTod() + "'.");
        }

                User user = userBusiness.findOrCreateUser(userVO);
        Address address = addressBusiness.findOrCreateAddressEntity(addressVO);

                GoatFarm goatFarm = goatFarmMapper.toEntity(farmVO);
        goatFarm.setUser(user);
        goatFarm.setAddress(address);

                List<Phone> associatedPhones = new ArrayList<>();
        Set<String> processedNumbers = new HashSet<>();
        for (PhoneRequestVO phoneVO : phoneVOs) {
            if (!processedNumbers.add(phoneVO.getNumber())) {
                throw new DuplicateEntityException("NÃºmero de telefone duplicado: " + phoneVO.getNumber());
            }
            if (phoneBusiness.existsByDddAndNumber(phoneVO.getDdd(), phoneVO.getNumber())) {
                throw new DuplicateEntityException("JÃ¡ existe um telefone com DDD (" + phoneVO.getDdd() + ") e nÃºmero " + phoneVO.getNumber());
            }
            Phone phone = new Phone();
            phone.setDdd(phoneVO.getDdd());
            phone.setNumber(phoneVO.getNumber());
            phone.setGoatFarm(goatFarm);
            associatedPhones.add(phone);
        }
        goatFarm.setPhones(associatedPhones);

        try {
            GoatFarm savedFarm = goatFarmDAO.save(goatFarm);
            return goatFarmMapper.toFullResponseVO(savedFarm);
        } catch (DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException("Erro de integridade de dados ao salvar a fazenda: " + e.getMessage(), e);
        }
    }

        @Transactional
    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {
        User user = userBusiness.getUserEntityById(requestVO.getUserId());
        Address address = addressBusiness.getAddressEntityById(requestVO.getAddressId());
        
        GoatFarm goatFarm = GoatFarm.builder()
                .name(requestVO.getName())
                .user(user)
                .address(address)
                .build();
        
        List<Phone> phones = requestVO.getPhoneIds().stream()
                .map(phoneBusiness::getPhoneEntityById)
                .collect(Collectors.toList());
        
        goatFarm.setPhones(phones);
        
        GoatFarm savedGoatFarm = goatFarmDAO.save(goatFarm);
        return goatFarmMapper.toFullResponseVO(savedGoatFarm);
    }

    public GoatFarmFullResponseVO createGoatFarmWithExistingIds(GoatFarmRequestVO requestVO) {
        User user = userBusiness.getUserEntityById(requestVO.getUserId());
        Address address = addressBusiness.getAddressEntityById(requestVO.getAddressId());
        
        GoatFarm goatFarm = GoatFarm.builder()
                .name(requestVO.getName())
                .user(user)
                .address(address)
                .build();
        
        List<Phone> phones = requestVO.getPhoneIds().stream()
                .map(phoneBusiness::getPhoneEntityById)
                .collect(Collectors.toList());
        
        goatFarm.setPhones(phones);
        
        GoatFarm savedGoatFarm = goatFarmDAO.save(goatFarm);
        return goatFarmMapper.toFullResponseVO(savedGoatFarm);
    }

    @Transactional
    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO farmVO,
                                                 UserRequestVO userVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {
        GoatFarm goatFarmToUpdate = goatFarmDAO.findFarmEntityById(id);

        if (!goatFarmToUpdate.getName().equals(farmVO.getName()) && goatFarmDAO.existsByName(farmVO.getName())) {
            throw new DuplicateEntityException("JÃ¡ existe outra fazenda com o nome '" + farmVO.getName() + "'.");
        }
        if (farmVO.getTod() != null && (goatFarmToUpdate.getTod() == null || !goatFarmToUpdate.getTod().equals(farmVO.getTod()))
                && goatFarmDAO.existsByTod(farmVO.getTod())) {
            throw new DuplicateEntityException("JÃ¡ existe outra fazenda com o cÃ³digo '" + farmVO.getTod() + "'.");
        }

                userBusiness.updateUser(farmVO.getUserId(), userVO);
        addressBusiness.updateAddress(farmVO.getAddressId(), addressVO);

        if (phoneVOs == null || phoneVOs.isEmpty()) {
            throw new IllegalArgumentException("Ã‰ obrigatÃ³rio informar ao menos um telefone.");
        }
        for (PhoneRequestVO phoneVO : phoneVOs) {
            phoneBusiness.updatePhone(phoneVO.getId(), phoneVO);
        }

        List<Long> phoneIds = phoneVOs.stream().map(PhoneRequestVO::getId).toList();
        List<Phone> phones = phoneBusiness.findAllEntitiesById(phoneIds);
        if (phones.size() != phoneIds.size()) {
            throw new ResourceNotFoundException("Um ou mais telefones informados nÃ£o foram encontrados.");
        }

                goatFarmToUpdate.getPhones().removeIf(p -> !phoneIds.contains(p.getId()));
        Set<Long> currentPhoneIds = goatFarmToUpdate.getPhones().stream()
                .map(Phone::getId)
                .collect(java.util.stream.Collectors.toSet());
        for (Phone phone : phones) {
            if (!currentPhoneIds.contains(phone.getId())) {
                goatFarmToUpdate.getPhones().add(phone);
            }
        }

        goatFarmMapper.updateEntity(goatFarmToUpdate, farmVO);
        try {
            GoatFarm updatedFarm = goatFarmDAO.save(goatFarmToUpdate);
            return goatFarmMapper.toFullResponseVO(updatedFarm);
        } catch (DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException("Erro de integridade ao atualizar a fazenda: " + e.getMessage(), e);
        }
    }

        public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        return goatFarmDAO.findGoatFarmById(id);
    }

        public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        return goatFarmDAO.searchGoatFarmByName(name, pageable);
    }

        @Transactional(readOnly = true)
    public com.devmaster.goatfarm.farm.model.entity.GoatFarm getFarmEntityById(Long id) {
        return goatFarmDAO.findFarmEntityById(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteGoatFarmsFromOtherUsers(Long adminId) {
        goatFarmDAO.deleteGoatFarmsFromOtherUsers(adminId);
    }

        public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        return goatFarmDAO.findAllGoatFarm(pageable);
    }

        public String deleteGoatFarm(Long id) {
        return goatFarmDAO.deleteGoatFarm(id);
    }

    /**
     * Valida os dados de uma fazenda antes da criaÃ§Ã£o/atualizaÃ§Ã£o.
     */
    private void validateFarmData(GoatFarmRequestVO requestVO) {
        java.util.Map<String, String> validationErrors = new java.util.HashMap<>();
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados da fazenda para criaÃ§Ã£o nÃ£o podem ser nulos.");
        }
        if (requestVO.getName() != null) {
            String nome = requestVO.getName().trim();
            if (nome.length() < 3 || nome.length() > 100) {
                validationErrors.put("name", "Nome da fazenda deve ter entre 3 e 100 caracteres");
            }
        }
        if (!validationErrors.isEmpty()) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                    "Dados da fazenda invÃ¡lidos", validationErrors);
        }
    }
}
