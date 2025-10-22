package com.devmaster.goatfarm.farm.business.farmbusiness;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AddressRepository;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.phone.dao.PhoneDAO;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.phone.model.repository.PhoneRepository;
import com.devmaster.goatfarm.farm.converters.GoatFarmConverter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class GoatFarmBusiness {

    @Autowired
    private GoatFarmDAO goatFarmDAO;

    @Autowired private GoatFarmRepository goatFarmRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AddressBusiness addressBusiness;
    @Autowired private UserBusiness userBusiness;
    @Autowired private UserDAO userDAO;
    @Autowired private PhoneRepository phoneRepository;
    @Autowired private PhoneDAO phoneDAO;

    // ✅ Criação completa (fazenda + proprietário + endereço + telefones)
    @Transactional
    public GoatFarmFullResponseVO createFullGoatFarm(GoatFarmRequestVO farmVO,
                                                     UserRequestVO userVO,
                                                     AddressRequestVO addressVO,
                                                     List<PhoneRequestVO> phoneVOs) {
        if (farmVO == null) {
            throw new IllegalArgumentException("Os dados da fazenda para criação não podem ser nulos.");
        }
        if (userVO == null) {
            throw new IllegalArgumentException("Dados do usuário são obrigatórios.");
        }
        if (addressVO == null) {
            throw new IllegalArgumentException("Dados do endereço são obrigatórios.");
        }
        if (phoneVOs == null || phoneVOs.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone para a fazenda.");
        }

        if (goatFarmRepository.existsByName(farmVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + farmVO.getName() + "'.");
        }
        if (farmVO.getTod() != null && goatFarmRepository.existsByTod(farmVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + farmVO.getTod() + "'.");
        }

        // Cria/recupera usuário e endereço
        User user = userDAO.findOrCreateUser(userVO);
        Address address = addressBusiness.findOrCreateAddressEntity(addressVO);

        // Constrói a entidade de fazenda
        GoatFarm goatFarm = GoatFarmConverter.toEntity(farmVO, user, address);
        goatFarm.setAddress(address);

        // Validação e construção dos telefones
        List<Phone> associatedPhones = new ArrayList<>();
        Set<String> processedNumbers = new HashSet<>();
        for (PhoneRequestVO phoneVO : phoneVOs) {
            if (!processedNumbers.add(phoneVO.getNumber())) {
                throw new DuplicateEntityException("Número de telefone duplicado: " + phoneVO.getNumber());
            }
            if (phoneRepository.existsByDddAndNumber(phoneVO.getDdd(), phoneVO.getNumber())) {
                throw new DuplicateEntityException("Já existe um telefone com DDD (" + phoneVO.getDdd() + ") e número " + phoneVO.getNumber());
            }
            Phone phone = new Phone();
            phone.setDdd(phoneVO.getDdd());
            phone.setNumber(phoneVO.getNumber());
            phone.setGoatFarm(goatFarm);
            associatedPhones.add(phone);
        }
        goatFarm.setPhones(associatedPhones);

        try {
            GoatFarm savedFarm = goatFarmRepository.save(goatFarm);
            return GoatFarmConverter.toFullVO(savedFarm);
        } catch (DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException("Erro de integridade de dados ao salvar a fazenda: " + e.getMessage(), e);
        }
    }

    // Criação por IDs existentes
    @Transactional
    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {
        validateFarmData(requestVO);
        if (requestVO.getPhoneIds() == null || requestVO.getPhoneIds().isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone para a fazenda.");
        }
        if (goatFarmRepository.existsByName(requestVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + requestVO.getName() + "'.");
        }
        if (requestVO.getTod() != null && goatFarmRepository.existsByTod(requestVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + requestVO.getTod() + "'.");
        }

        User user = userRepository.findById(requestVO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + requestVO.getUserId()));
        Address address = addressRepository.findById(requestVO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado com o ID: " + requestVO.getAddressId()));

        List<Phone> phones = phoneRepository.findAllById(requestVO.getPhoneIds());
        if (phones.size() != requestVO.getPhoneIds().size()) {
            throw new ResourceNotFoundException("Um ou mais telefones informados não foram encontrados.");
        }
        for (Phone phone : phones) {
            if (phone.getGoatFarm() != null) {
                throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException(
                        "Telefone DDD (" + phone.getDdd() + ") número " + phone.getNumber() +
                        " já está associado à fazenda: " + phone.getGoatFarm().getName());
            }
        }

        GoatFarm goatFarm = GoatFarmConverter.toEntity(requestVO, user, address);
        goatFarm.setPhones(phones);
        try {
            goatFarm = goatFarmRepository.save(goatFarm);
            return GoatFarmConverter.toVO(goatFarm);
        } catch (DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException("Ocorreu um erro ao salvar a fazenda: " + e.getMessage());
        }
    }

    @Transactional
    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO farmVO,
                                                 UserRequestVO userVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {
        GoatFarm goatFarmToUpdate = goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada."));

        if (!goatFarmToUpdate.getName().equals(farmVO.getName()) && goatFarmRepository.existsByName(farmVO.getName())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o nome '" + farmVO.getName() + "'.");
        }
        if (farmVO.getTod() != null && (goatFarmToUpdate.getTod() == null || !goatFarmToUpdate.getTod().equals(farmVO.getTod()))
                && goatFarmRepository.existsByTod(farmVO.getTod())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o código '" + farmVO.getTod() + "'.");
        }

        // Atualiza User e Address via Business
        userBusiness.updateUser(farmVO.getUserId(), userVO);
        addressBusiness.updateAddress(farmVO.getAddressId(), addressVO);

        if (phoneVOs == null || phoneVOs.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone.");
        }
        for (PhoneRequestVO phoneVO : phoneVOs) {
            phoneDAO.updatePhone(phoneVO.getId(), phoneVO);
        }

        List<Long> phoneIds = phoneVOs.stream().map(PhoneRequestVO::getId).toList();
        List<Phone> phones = phoneRepository.findAllById(phoneIds);
        if (phones.size() != phoneIds.size()) {
            throw new ResourceNotFoundException("Um ou mais telefones informados não foram encontrados.");
        }

        // Atualiza coleção de telefones
        goatFarmToUpdate.getPhones().removeIf(p -> !phoneIds.contains(p.getId()));
        Set<Long> currentPhoneIds = goatFarmToUpdate.getPhones().stream()
                .map(Phone::getId)
                .collect(java.util.stream.Collectors.toSet());
        for (Phone phone : phones) {
            if (!currentPhoneIds.contains(phone.getId())) {
                goatFarmToUpdate.getPhones().add(phone);
            }
        }

        GoatFarmConverter.entityUpdate(goatFarmToUpdate, farmVO);
        try {
            GoatFarm updatedFarm = goatFarmRepository.save(goatFarmToUpdate);
            return GoatFarmConverter.toFullVO(updatedFarm);
        } catch (DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException("Erro de integridade ao atualizar a fazenda: " + e.getMessage(), e);
        }
    }

    // Busca por ID
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        return goatFarmDAO.findGoatFarmById(id);
    }

    // Busca por nome
    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        return goatFarmDAO.searchGoatFarmByName(name, pageable);
    }

    // Listar todas as fazendas
    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        return goatFarmDAO.findAllGoatFarm(pageable);
    }

    // Deleção
    public String deleteGoatFarm(Long id) {
        return goatFarmDAO.deleteGoatFarm(id);
    }

    /**
     * Valida os dados de uma fazenda antes da criação/atualização.
     */
    private void validateFarmData(GoatFarmRequestVO requestVO) {
        java.util.Map<String, String> validationErrors = new java.util.HashMap<>();
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados da fazenda para criação não podem ser nulos.");
        }
        if (requestVO.getName() != null) {
            String nome = requestVO.getName().trim();
            if (nome.length() < 3 || nome.length() > 100) {
                validationErrors.put("name", "Nome da fazenda deve ter entre 3 e 100 caracteres");
            }
        }
        if (!validationErrors.isEmpty()) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                    "Dados da fazenda inválidos", validationErrors);
        }
    }
}