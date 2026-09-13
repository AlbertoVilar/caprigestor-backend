package com.devmaster.goatfarm.farm.business;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.UserManagementUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.model.FarmPersistenceCommand;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.in.GoatFarmManagementUseCase;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.business.bo.*;
import com.devmaster.goatfarm.farm.business.mapper.FarmBusinessMapper;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.phoneservice.PhoneBusiness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GoatFarmBusiness implements GoatFarmManagementUseCase {
    private final GoatFarmPersistencePort goatFarmPort; private final AddressBusiness addressBusiness; private final UserManagementUseCase userManagement;
    private final PhoneBusiness phoneBusiness; private final FarmBusinessMapper mapper; private final FarmAuthorizationUseCase authorization; private final CurrentPrincipalQueryUseCase principalQuery;
    public GoatFarmBusiness(GoatFarmPersistencePort goatFarmPort, AddressBusiness addressBusiness, UserManagementUseCase userManagement,
                            PhoneBusiness phoneBusiness, FarmBusinessMapper mapper, FarmAuthorizationUseCase authorization, CurrentPrincipalQueryUseCase principalQuery) {
        this.goatFarmPort = goatFarmPort; this.addressBusiness = addressBusiness; this.userManagement = userManagement; this.phoneBusiness = phoneBusiness; this.mapper = mapper; this.authorization = authorization; this.principalQuery = principalQuery;
    }
    @Transactional(readOnly = true) public GoatFarmFullResponseVO findGoatFarmById(Long id) { return mapper.toFullResponseVO(require(id, true)); }
    @Transactional(readOnly = true) public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) { return goatFarmPort.searchByName(name, pageable).map(mapper::toFullResponseVO); }
    @Transactional(readOnly = true) public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) { return goatFarmPort.findAll(pageable).map(mapper::toFullResponseVO); }
    @Transactional public void deleteGoatFarm(Long id) { authorization.verifyFarmOwnership(id); require(id, false); goatFarmPort.deleteById(id); }

    @Transactional
    public GoatFarmFullResponseVO createGoatFarm(GoatFarmFullRequestVO request) {
        AuthenticatedPrincipal current = principalQuery.findCurrent().orElse(null); validateCreation(request, current);
        GoatFarmRequestVO farm = request.getFarm(); validateLogoUrl(farm.getLogoUrl());
        if (goatFarmPort.existsByName(farm.getName())) throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + farm.getName() + "'.");
        if (farm.getTod() != null && goatFarmPort.existsByTod(farm.getTod())) throw new DuplicateEntityException("Já existe uma fazenda com o código '" + farm.getTod() + "'.");
        OwnerData owner = resolveOwner(request.getUser(), current);
        var address = addressBusiness.findOrCreateAddress(request.getAddress());
        try {
            FarmRecord saved = goatFarmPort.save(new FarmPersistenceCommand(null, farm.getName(), farm.getTod(), farm.getLogoUrl(), owner.id(), address.getId(), farm.getVersion()));
            phoneBusiness.createPhones(saved.id(), request.getPhones());
            return mapper.toFullResponseVO(require(saved.id(), true));
        } catch (DataIntegrityViolationException e) { throw new DuplicateEntityException("Não foi possível processar a solicitação devido a conflito de dados."); }
    }

    @Transactional
    public GoatFarmFullResponseVO updateGoatFarm(Long id, GoatFarmRequestVO farmVO, UserRequestVO userVO, AddressRequestVO addressVO, List<PhoneRequestVO> phones) {
        authorization.verifyFarmOwnership(id); FarmRecord current = require(id, false);
        String name = farmVO != null && farmVO.getName() != null ? farmVO.getName() : current.name(); String tod = farmVO != null ? farmVO.getTod() : current.tod(); String logo = farmVO != null ? farmVO.getLogoUrl() : current.logoUrl();
        if (farmVO != null) { if (!name.equals(current.name()) && goatFarmPort.existsByName(name)) throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + name + "'."); if (tod != null && !tod.equals(current.tod()) && goatFarmPort.existsByTod(tod)) throw new DuplicateEntityException("Já existe uma fazenda com o código '" + tod + "'."); validateLogoUrl(logo); }
        if (userVO != null) { if (current.ownerUserId() == null) throw new InvalidArgumentException("user", "Usuário proprietário não encontrado para esta fazenda."); userManagement.updateUser(current.ownerUserId(), userVO); }
        Long addressId = current.addressId(); if (addressVO != null) { if (addressVO.getId() == null && addressId == null) addressId = addressBusiness.findOrCreateAddress(addressVO).getId(); else { addressVO.setId(addressVO.getId() == null ? addressId : addressVO.getId()); addressId = addressBusiness.updateAddress(id, addressVO.getId(), addressVO).getId(); } }
        if (phones == null || phones.isEmpty()) throw new InvalidArgumentException("phones", "É obrigatório informar ao menos um telefone.");
        goatFarmPort.save(new FarmPersistenceCommand(id, name, tod, logo, current.ownerUserId(), addressId, current.version())); phoneBusiness.replacePhones(id, phones);
        return mapper.toFullResponseVO(require(id, true));
    }
    @Transactional(readOnly = true) public FarmPermissionsVO getFarmPermissions(Long farmId) { require(farmId, false); return new FarmPermissionsVO(authorization.canManageFarm(farmId), authorization.canAdministerFarm(farmId)); }

    private FarmRecord require(Long id, boolean details) { Optional<?> found = details ? goatFarmPort.findByIdWithDetails(id) : goatFarmPort.findById(id); return (FarmRecord) found.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada com ID: " + id)); }
    private OwnerData resolveOwner(UserRequestVO user, AuthenticatedPrincipal current) {
        if (current != null) return new OwnerData(current.id());
        if (user.getRoles() != null && !user.getRoles().isEmpty()) throw new BusinessRuleException("Não é permitido definir permissões (roles) no cadastro público.");
        if (userManagement.findByEmail(user.getEmail()) != null) throw new DuplicateEntityException("Não foi possível completar o cadastro com os dados informados.");
        user.setRoles(List.of("ROLE_FARM_OWNER")); AuthorityAccount account = userManagement.findOrCreateUser(user); return new OwnerData(account.id());
    }
    private void validateCreation(GoatFarmFullRequestVO request, AuthenticatedPrincipal current) { if (request == null || request.getFarm() == null) throw new InvalidArgumentException("farm", "Dados da fazenda são obrigatórios."); if (current == null && request.getUser() == null) throw new InvalidArgumentException("user", "Dados do usuário são obrigatórios para cadastro público."); if (request.getAddress() == null) throw new InvalidArgumentException("address", "Dados de endereço são obrigatórios."); if (request.getPhones() == null || request.getPhones().isEmpty()) throw new InvalidArgumentException("phones", "É obrigatório informar ao menos um telefone."); }
    private void validateLogoUrl(String value) { if (value == null) return; String normalized = value.trim().toLowerCase(); if (value.isBlank() || value.length() > 1000 || (!normalized.startsWith("http://") && !normalized.startsWith("https://"))) throw new InvalidArgumentException("logoUrl", "URL do logo inválida."); }
    private record OwnerData(Long id) { }
}
