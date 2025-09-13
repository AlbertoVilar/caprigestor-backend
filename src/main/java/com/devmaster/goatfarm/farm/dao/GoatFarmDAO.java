package com.devmaster.goatfarm.farm.dao;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GoatFarmDAO {

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private PhoneDAO phoneDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public GoatFarmFullResponseVO createFullGoatFarm(GoatFarmRequestVO farmRequestVO,
                                                     UserRequestVO userRequestVO,
                                                     AddressRequestVO addressRequestVO,
                                                     List<PhoneRequestVO> phoneRequestVOs) {
        if (farmRequestVO == null) {
            throw new IllegalArgumentException("Os dados da fazenda para criação não podem ser nulos.");
        }

        if (userRequestVO == null) {
            throw new IllegalArgumentException("Dados do usuário são obrigatórios.");
        }

        if (addressRequestVO == null) {
            throw new IllegalArgumentException("Dados do endereço são obrigatórios.");
        }

        if (phoneRequestVOs == null || phoneRequestVOs.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone para a fazenda.");
        }

        // Verificações duplicadas
        if (goatFarmRepository.existsByName(farmRequestVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + farmRequestVO.getName() + "'.");
        }

        if (farmRequestVO.getTod() != null && goatFarmRepository.existsByTod(farmRequestVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + farmRequestVO.getTod() + "'.");
        }

        // 1. Usuário (pode ser existente ou novo)
        User user = userDAO.findOrCreateUser(userRequestVO);

        // 2. Criar novo endereço usando o AddressDAO
        Address address = addressDAO.findOrCreateAddress(addressRequestVO);

        // 3. Criar a fazenda e associar o endereço
        GoatFarm goatFarm = GoatFarmConverter.toEntity(farmRequestVO, user, address);
        goatFarm.setAddress(address);
        
        // 4. Telefones - Criar novos telefones com referência bidirecional
        List<Phone> associatedPhones = new ArrayList<>();
        Set<String> processedNumbers = new HashSet<>();

        for (PhoneRequestVO phoneVO : phoneRequestVOs) {
            if (!processedNumbers.add(phoneVO.getNumber())) {
                throw new DuplicateEntityException("Número de telefone duplicado: " + phoneVO.getNumber());
            }

            // Verificar se já existe telefone com este DDD e número
            if (phoneRepository.existsByDddAndNumber(phoneVO.getDdd(), phoneVO.getNumber())) {
                throw new DuplicateEntityException("Já existe um telefone com DDD (" + phoneVO.getDdd() + ") e número " + phoneVO.getNumber());
            }

            // Criar novo telefone
            Phone phone = new Phone();
            phone.setDdd(phoneVO.getDdd());
            phone.setNumber(phoneVO.getNumber());
            phone.setGoatFarm(goatFarm); // Referência bidirecional
            associatedPhones.add(phone);
        }

        // 5. Associa a lista de telefones à fazenda (referência bidirecional)
        goatFarm.setPhones(associatedPhones);

        // 6. Salva APENAS a fazenda. O Cascade fará o resto.
        try {
            GoatFarm savedFarm = goatFarmRepository.save(goatFarm);
            return GoatFarmConverter.toFullVO(savedFarm);
        } catch (DataIntegrityViolationException e) {
            // Este catch agora pegará erros de constraint do banco de forma mais limpa
            throw new DatabaseException("Erro de integridade de dados ao salvar a fazenda: " + e.getMessage(), e);
        }
    }
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
                throw new DatabaseException("Telefone DDD (" + phone.getDdd() + ") número " + phone.getNumber()
                        + " já está associado à fazenda: " + phone.getGoatFarm().getName());
            }
        }

        GoatFarm goatFarm = GoatFarmConverter.toEntity(requestVO, user, address);
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
                                                 UserRequestVO userVO,
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
        userDAO.updateUser(farmVO.getUserId(), userVO);
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
