package com.devmaster.goatfarm.farm.dao;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AddressRepository;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(GoatFarmDAO.class);

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserBusiness userBusiness;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private AddressBusiness addressBusiness;

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
        throw new IllegalStateException("Operação movida para camada Business. Use GoatFarmBusiness.createFullGoatFarm.");
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

        logger.info("Iniciando atualização da fazenda com ID: {}", id);
        
        // Validação de existência da fazenda
        GoatFarm goatFarmToUpdate = goatFarmRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Fazenda com ID {} não encontrada", id);
                    return new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada.");
                });

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

        // Update related entities
        logger.debug("Atualizando usuário com ID: {}", farmVO.getUserId());
        try {
            userBusiness.updateUser(farmVO.getUserId(), userVO);
            logger.debug("Usuário atualizado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao atualizar usuário com ID {}: {}", farmVO.getUserId(), e.getMessage());
            throw e;
        }
        
        logger.debug("Atualizando endereço com ID: {}", farmVO.getAddressId());
        try {
            addressBusiness.updateAddress(farmVO.getAddressId(), addressVO);
            logger.debug("Endereço atualizado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao atualizar endereço com ID {}: {}", farmVO.getAddressId(), e.getMessage());
            throw e;
        }

        // Validação de telefones
        if (phoneVOs == null || phoneVOs.isEmpty()) {
            logger.error("Lista de telefones vazia ou nula para fazenda ID: {}", id);
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone.");
        }
        
        logger.debug("Atualizando {} telefones", phoneVOs.size());
        // Update phones individually
        for (PhoneRequestVO phoneVO : phoneVOs) {
            try {
                logger.debug("Atualizando telefone com ID: {}", phoneVO.getId());
                phoneDAO.updatePhone(phoneVO.getId(), phoneVO);
            } catch (Exception e) {
                logger.error("Erro ao atualizar telefone com ID {}: {}", phoneVO.getId(), e.getMessage());
                throw e;
            }
        }
        logger.debug("Todos os telefones atualizados com sucesso");

        // Find Phone entities and update in the farm
        logger.debug("Buscando entidades de telefone no banco de dados");
        List<Long> phoneIds = phoneVOs.stream().map(PhoneRequestVO::getId).toList();
        List<Phone> phones = phoneRepository.findAllById(phoneIds);

        if (phones.size() != phoneIds.size()) {
            logger.error("Telefones não encontrados. Esperados: {}, Encontrados: {}", phoneIds.size(), phones.size());
            throw new ResourceNotFoundException("Um ou mais telefones informados não foram encontrados.");
        }
        
        logger.debug("Atualizando coleção de telefones da fazenda");
        // ✅ Correção: Atualização segura de telefones sem usar clear()
        // Remove telefones que não estão mais na lista
        int removedCount = goatFarmToUpdate.getPhones().size();
        goatFarmToUpdate.getPhones().removeIf(phone -> !phoneIds.contains(phone.getId()));
        removedCount = removedCount - goatFarmToUpdate.getPhones().size();
        logger.debug("Removidos {} telefones da coleção", removedCount);
        
        // Adiciona apenas telefones que ainda não estão na coleção
        Set<Long> currentPhoneIds = goatFarmToUpdate.getPhones().stream()
                .map(Phone::getId)
                .collect(java.util.stream.Collectors.toSet());
        
        int addedCount = 0;
        for (Phone phone : phones) {
            if (!currentPhoneIds.contains(phone.getId())) {
                goatFarmToUpdate.getPhones().add(phone);
                addedCount++;
            }
        }
        logger.debug("Adicionados {} telefones à coleção", addedCount);

        // Update the farm data itself
        GoatFarmConverter.entityUpdate(goatFarmToUpdate, farmVO);

        try {
            logger.info("Salvando fazenda atualizada no banco de dados");
            GoatFarm updatedFarm = goatFarmRepository.save(goatFarmToUpdate);
            logger.info("Fazenda com ID {} atualizada com sucesso. Nome: {}, TOD: {}", 
                       id, updatedFarm.getName(), updatedFarm.getTod());

            // ✅ Retorna dados completos
            return GoatFarmConverter.toFullVO(updatedFarm);

        } catch (DataIntegrityViolationException e) {
            logger.error("Erro de integridade de dados ao salvar fazenda ID {}: {}", id, e.getMessage());
            logger.error("Causa raiz da violação de integridade: {}", e.getRootCause() != null ? e.getRootCause().getMessage() : "N/A");
            throw new DatabaseException("Erro de integridade ao atualizar a fazenda: " + e.getMessage(), e);
        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("Erro de acesso aos dados ao salvar fazenda ID {}: {}", id, e.getMessage());
            throw new DatabaseException("Erro de acesso aos dados ao atualizar a fazenda: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar fazenda ID {}: {} - Tipo: {}", id, e.getMessage(), e.getClass().getSimpleName());
            logger.error("Stack trace completo:", e);
            throw new DatabaseException("Erro inesperado ao atualizar a fazenda com ID " + id + ": " + e.getMessage(), e);
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
        logger.info("Tentando deletar fazenda com ID: {}", id);
        
        if (!goatFarmRepository.existsById(id)) {
            logger.warn("Tentativa de deletar fazenda inexistente com ID: {}", id);
            throw new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada.");
        }
        
        try {
            goatFarmRepository.deleteById(id);
            logger.info("Fazenda com ID {} deletada com sucesso", id);
            return "Fazenda com ID " + id + " foi deletada com sucesso.";
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro de integridade ao deletar fazenda com ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Não é possível deletar a fazenda com ID " + id +
                    " porque ela possui relacionamentos com outras entidades.", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao deletar fazenda com ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Erro inesperado ao deletar a fazenda com ID " + id, e);
        }
    }
}
