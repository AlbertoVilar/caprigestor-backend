package com.devmaster.goatfarm.farm.business.farmbusiness;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.config.security.OwnershipService;
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
public class GoatFarmBusiness {

    private final GoatFarmDAO goatFarmDAO;
    private final AddressBusiness addressBusiness;
    private final UserBusiness userBusiness;
    private final PhoneBusiness phoneBusiness;
    private final GoatFarmMapper goatFarmMapper;
    private final PhoneMapper phoneMapper;
    private final OwnershipService ownershipService;

    @Autowired
    public GoatFarmBusiness(
            GoatFarmDAO goatFarmDAO,
            AddressBusiness addressBusiness,
            UserBusiness userBusiness,
            PhoneBusiness phoneBusiness,
            GoatFarmMapper goatFarmMapper,
            PhoneMapper phoneMapper,
            OwnershipService ownershipService
    ) {
        this.goatFarmDAO = goatFarmDAO;
        this.addressBusiness = addressBusiness;
        this.userBusiness = userBusiness;
        this.phoneBusiness = phoneBusiness;
        this.goatFarmMapper = goatFarmMapper;
        this.phoneMapper = phoneMapper;
        this.ownershipService = ownershipService;
    }

    @Transactional
    public GoatFarmFullResponseVO createFullGoatFarm(GoatFarmRequestVO farmVO,
                                                     UserRequestVO userVO,
                                                     AddressRequestVO addressVO,
                                                     List<PhoneRequestVO> phoneVOs) {
        if (farmVO == null) {
            throw new IllegalArgumentException("Dados da fazenda são obrigatórios.");
        }
        if (userVO == null) {
            throw new IllegalArgumentException("Dados do usuário são obrigatórios.");
        }
        if (addressVO == null) {
            throw new IllegalArgumentException("Dados de endereço são obrigatórios.");
        }
        if (phoneVOs == null || phoneVOs.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone.");
        }

        if (goatFarmDAO.existsByName(farmVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + farmVO.getName() + "'.");
        }
        if (farmVO.getTod() != null && goatFarmDAO.existsByTod(farmVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + farmVO.getTod() + "'.");
        }

        User owner = userBusiness.findOrCreateUser(userVO);
        var addressEntity = addressBusiness.findOrCreateAddressEntity(addressVO);

        GoatFarm farmEntity = goatFarmMapper.toEntity(farmVO);
        farmEntity.setUser(owner);
        farmEntity.setAddress(addressEntity);

        try {
            GoatFarm savedFarm = goatFarmDAO.save(farmEntity);
            phoneBusiness.createPhones(savedFarm.getId(), phoneVOs);
            GoatFarm reloaded = goatFarmDAO.findFarmEntityById(savedFarm.getId());
            return goatFarmMapper.toFullResponseVO(reloaded);
        } catch (DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException("Erro ao criar fazenda: " + e.getMessage(), e);
        }
    }

    @Transactional
    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {
        if (requestVO == null) {
            throw new IllegalArgumentException("Dados da fazenda são obrigatórios.");
        }

        if (goatFarmDAO.existsByName(requestVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + requestVO.getName() + "'.");
        }
        if (requestVO.getTod() != null && goatFarmDAO.existsByTod(requestVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + requestVO.getTod() + "'.");
        }

        GoatFarm entity = goatFarmMapper.toEntity(requestVO);
        User currentUser = ownershipService.getCurrentUser();
        entity.setUser(currentUser);

        try {
            GoatFarm saved = goatFarmDAO.save(entity);
            return goatFarmMapper.toResponseVO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException("Erro ao criar fazenda: " + e.getMessage(), e);
        }
    }

    @Transactional
    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO farmVO,
                                                 UserRequestVO userVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {
        GoatFarm goatFarmToUpdate = goatFarmDAO.findFarmEntityById(id);

        // Verificação de concorrência otimista: compara a versão enviada com a atual
        Integer requestedVersion = farmVO.getVersion();
        Integer currentVersion = goatFarmToUpdate.getVersion();
        if (requestedVersion == null || !requestedVersion.equals(currentVersion)) {
            throw new org.springframework.dao.OptimisticLockingFailureException(
                    "Registro desatualizado: versão atual " + currentVersion + ", versão informada " + requestedVersion);
        }

        if (!goatFarmToUpdate.getName().equals(farmVO.getName()) && goatFarmDAO.existsByName(farmVO.getName())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o nome '" + farmVO.getName() + "'.");
        }
        if (farmVO.getTod() != null && (goatFarmToUpdate.getTod() == null || !goatFarmToUpdate.getTod().equals(farmVO.getTod()))
                && goatFarmDAO.existsByTod(farmVO.getTod())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o código '" + farmVO.getTod() + "'.");
        }

        userBusiness.updateUser(goatFarmToUpdate.getUser().getId(), userVO);
        // Passa farmId explicitamente para verificação de ownership e resolução correta
        addressBusiness.updateAddress(goatFarmToUpdate.getId(), goatFarmToUpdate.getAddress().getId(), addressVO);

        if (phoneVOs == null || phoneVOs.isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar ao menos um telefone.");
        }
        for (PhoneRequestVO phoneVO : phoneVOs) {
            // Atualiza telefone com validação de propriedade por fazenda
            phoneBusiness.updatePhone(goatFarmToUpdate.getId(), phoneVO.getId(), phoneVO);
        }

        List<Long> phoneIds = phoneVOs.stream().map(PhoneRequestVO::getId).toList();
        List<Phone> phones = phoneBusiness.findAllEntitiesById(phoneIds);
        if (phones.size() != phoneIds.size()) {
            throw new ResourceNotFoundException("Um ou mais telefones informados não foram encontrados.");
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

    // ... (outros métodos mantidos como estão)

    @Transactional(readOnly = true)
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        return goatFarmDAO.findGoatFarmById(id);
    }

    @Transactional(readOnly = true)
    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        return goatFarmDAO.searchGoatFarmByName(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        return goatFarmDAO.findAllGoatFarm(pageable);
    }

    @Transactional
    public void deleteGoatFarm(Long id) {
        goatFarmDAO.deleteGoatFarm(id);
    }

    @Transactional
    public void deleteGoatFarmsFromOtherUsers(Long adminId) {
        goatFarmDAO.deleteGoatFarmsFromOtherUsers(adminId);
    }
}
