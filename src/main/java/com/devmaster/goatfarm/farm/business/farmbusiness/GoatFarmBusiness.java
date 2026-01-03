package com.devmaster.goatfarm.farm.business.farmbusiness;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.business.bo.FarmPermissionsVO;
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

import java.time.Instant;
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

    @Transactional
    public GoatFarmFullResponseVO createGoatFarm(GoatFarmFullRequestVO fullRequestVO) {
        User currentUser = null;
        try {
            currentUser = ownershipService.getCurrentUser();
        } catch (UnauthorizedException e) {
            // Usuário anônimo
        }

        validateGoatFarmCreation(fullRequestVO, currentUser);

        GoatFarmRequestVO farmVO = fullRequestVO.getFarm();
        UserRequestVO userVO = fullRequestVO.getUser();
        AddressRequestVO addressVO = fullRequestVO.getAddress();
        List<PhoneRequestVO> phoneVOs = fullRequestVO.getPhones();

        if (goatFarmDAO.existsByName(farmVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + farmVO.getName() + "'.");
        }
        if (farmVO.getTod() != null && goatFarmDAO.existsByTod(farmVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + farmVO.getTod() + "'.");
        }

        User owner = resolveOwner(userVO, currentUser);
        var addressEntity = addressBusiness.findOrCreateAddressEntity(addressVO);

        GoatFarm farmEntity = goatFarmMapper.toEntity(farmVO);
        farmEntity.setUser(owner);
        farmEntity.setAddress(addressEntity);

        try {
            GoatFarm savedFarm = goatFarmDAO.save(farmEntity);
            phoneBusiness.createPhones(savedFarm.getId(), phoneVOs);
            
            // Recarrega para garantir relacionamentos atualizados
            GoatFarm reloaded = goatFarmDAO.findFarmEntityById(savedFarm.getId());
            return goatFarmMapper.toFullResponseVO(reloaded);
        } catch (DataIntegrityViolationException e) {
            // Mensagem genérica para o cliente, detalhe na causa (logs)
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException("Não foi possível processar a solicitação devido a conflito de dados.", e);
        }
    }

    @Transactional
    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO farmVO,
                                                 UserRequestVO userVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {
        // Verifica propriedade da fazenda
        ownershipService.verifyFarmOwnership(id);

        GoatFarm farmEntity = goatFarmDAO.findFarmEntityById(id);

        // Atualiza dados básicos da fazenda
        if (farmVO != null) {
            // Valida duplicidade de nome/tod quando fornecidos
            if (farmVO.getName() != null && !farmVO.getName().equals(farmEntity.getName()) && goatFarmDAO.existsByName(farmVO.getName())) {
                throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + farmVO.getName() + "'.");
            }
            if (farmVO.getTod() != null && !farmVO.getTod().equals(farmEntity.getTod()) && goatFarmDAO.existsByTod(farmVO.getTod())) {
                throw new DuplicateEntityException("Já existe uma fazenda com o código '" + farmVO.getTod() + "'.");
            }
            goatFarmMapper.updateEntity(farmEntity, farmVO);
        }

        // Atualiza/associa usuário
        if (userVO != null) {
            User owner = userBusiness.findOrCreateUser(userVO);
            farmEntity.setUser(owner);
        }

        // Atualiza/associa endereço
        if (addressVO != null) {
            var addressEntity = addressBusiness.findOrCreateAddressEntity(addressVO);
            farmEntity.setAddress(addressEntity);
        }

        // Observação: atualização de telefones pode ser orquestrada no PhoneBusiness.
        // Para manter escopo enxuto e compilar, persistimos alterações básicas.

        GoatFarm saved = goatFarmDAO.save(farmEntity);
        // Recarrega para garantir relacionamentos atualizados
        GoatFarm reloaded = goatFarmDAO.findFarmEntityById(saved.getId());
        return goatFarmMapper.toFullResponseVO(reloaded);
    }

    @Transactional(readOnly = true)
    public FarmPermissionsVO getFarmPermissions(Long farmId) {
        User current = ownershipService.getCurrentUser();
        boolean isAdmin = ownershipService.isCurrentUserAdmin();
        GoatFarm farm = goatFarmDAO.findFarmEntityById(farmId);
        boolean isOwner = farm.getUser() != null && farm.getUser().getId().equals(current.getId());
        return new FarmPermissionsVO(isAdmin || isOwner);
    }

    private User resolveOwner(UserRequestVO userVO, User currentUser) {
        if (currentUser != null) {
            // Se existe usuário autenticado, ele será o owner.
            return currentUser;
        }

        // Fluxo anônimo (Registration)
        // userVO validado em validateGoatFarmCreation
        
        // Validação estrita: Não pode definir roles
        if (userVO.getRoles() != null && !userVO.getRoles().isEmpty()) {
            throw new ValidationException(new ValidationError(Instant.now(), 400, "Não é permitido definir permissões (roles) no cadastro público.", null));
        }

        // Garante que não estamos vinculando a um usuário existente (Segurança/IDOR)
        if (userBusiness.findUserByEmail(userVO.getEmail()).isPresent()) {
            // Mensagem genérica para evitar enumeração de usuários
            throw new DuplicateEntityException("Não foi possível completar o cadastro com os dados informados.");
        }
        
        // Define role padrão ROLE_USER
        userVO.setRoles(java.util.List.of("ROLE_USER"));
        
        // Cria novo usuário
        return userBusiness.findOrCreateUser(userVO);
    }

    private void validateGoatFarmCreation(GoatFarmFullRequestVO fullRequestVO, User currentUser) {
        ValidationError validationError = new ValidationError(Instant.now(), 422, "Erro de validação", null);

        if (fullRequestVO.getFarm() == null) {
            validationError.addError("farm", "Dados da fazenda são obrigatórios.");
        }
        
        // Validação condicional do usuário
        if (currentUser == null) {
            // Anônimo: userVO é obrigatório
            if (fullRequestVO.getUser() == null) {
                validationError.addError("user", "Dados do usuário são obrigatórios para cadastro público.");
            }
        }

        if (fullRequestVO.getAddress() == null) {
            validationError.addError("address", "Dados de endereço são obrigatórios.");
        }
        if (fullRequestVO.getPhones() == null || fullRequestVO.getPhones().isEmpty()) {
            validationError.addError("phones", "É obrigatório informar ao menos um telefone.");
        }

        if (!validationError.getErrors().isEmpty()) {
            throw new ValidationException(validationError);
        }
    }
}