package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressBusiness {

    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private OwnershipService ownershipService;

    public AddressResponseVO createAddress(Long farmId, AddressRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        Map<String, String> validationErrors = validateAddressData(requestVO);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Dados de endereço inválidos: " + validationErrors);
        }
        Address entity = addressMapper.toEntity(requestVO);
        Address saved = addressDAO.createAddress(entity);
        return addressMapper.toResponseVO(saved);
    }

    public AddressResponseVO updateAddress(Long farmId, Long addressId, AddressRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        Map<String, String> validationErrors = validateAddressData(requestVO);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Dados de endereço inválidos: " + validationErrors);
        }
        Address current = addressDAO.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        addressMapper.toEntity(current, requestVO);
        Address updated = addressDAO.updateAddress(addressId, current);
        return addressMapper.toResponseVO(updated);
    }

    public AddressResponseVO findAddressById(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        Address entity = addressDAO.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        return addressMapper.toResponseVO(entity);
    }

    @Transactional(readOnly = true)
    public Address getAddressEntityById(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        return addressDAO.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
    }

    public String deleteAddress(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        Address entity = addressDAO.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        return addressDAO.deleteAddress(addressId);
    }

    public Address findOrCreateAddressEntity(AddressRequestVO requestVO) {
        Map<String, String> validationErrors = validateAddressData(requestVO);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Dados de endereço inválidos: " + validationErrors);
        }
        return addressDAO.searchExactAddress(
                        requestVO.getStreet(),
                        requestVO.getNeighborhood(),
                        requestVO.getCity(),
                        requestVO.getState(),
                        requestVO.getZipCode()
                )
                .orElseGet(() -> addressDAO.createAddress(addressMapper.toEntity(requestVO)));
    }

    private Map<String, String> validateAddressData(AddressRequestVO requestVO) {
        Map<String, String> validationErrors = new HashMap<>();
        if (requestVO.getZipCode() != null) {
            String cep = requestVO.getZipCode().replaceAll("[^0-9]", "");
            if (!cep.matches("^\\d{8}$")) {
                validationErrors.put("zipCode", "CEP deve conter exatamente 8 dígitos numéricos");
            }
        }
        if (requestVO.getState() != null) {
            if (!isValidBrazilianState(requestVO.getState())) {
                validationErrors.put("state", "Estado deve ser uma sigla válida (ex: SP, RJ, MG) ou nome completo (ex: São Paulo, Rio de Janeiro, Minas Gerais)");
            }
        }
        if (requestVO.getCountry() != null &&
                !requestVO.getCountry().trim().equalsIgnoreCase("Brasil") &&
                !requestVO.getCountry().trim().equalsIgnoreCase("Brazil")) {
            validationErrors.put("country", "Por enquanto, apenas endereços do Brasil são aceitos");
        }
        return validationErrors;
    }

    private boolean isValidBrazilianState(String state) {
        String[] siglasValidas = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
                "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
                "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};
        String[] nomesValidos = {"Acre", "Alagoas", "Amapá", "Amazonas", "Bahia", "Ceará",
                "Distrito Federal", "Espírito Santo", "Goiás", "Maranhão",
                "Mato Grosso", "Mato Grosso do Sul", "Minas Gerais", "Pará",
                "Paraíba", "Paraná", "Pernambuco", "Piauí", "Rio de Janeiro",
                "Rio Grande do Norte", "Rio Grande do Sul", "Rondônia",
                "Roraima", "Santa Catarina", "São Paulo", "Sergipe", "Tocantins"};

        String estadoInput = state.trim();
        for (String sigla : siglasValidas) {
            if (sigla.equalsIgnoreCase(estadoInput)) {
                return true;
            }
        }
        for (String nome : nomesValidos) {
            if (nome.equalsIgnoreCase(estadoInput)) {
                return true;
            }
        }
        return false;
    }
}