package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import com.devmaster.goatfarm.address.model.entity.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressBusiness {

    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private AddressMapper addressMapper;

    public AddressResponseVO createAddress(AddressRequestVO requestVO) {
        Map<String, String> validationErrors = validateAddressData(requestVO);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Dados de endereço inválidos: " + validationErrors);
        }
        Address entity = addressMapper.toEntity(requestVO);
        Address saved = addressDAO.createAddress(entity);
        return addressMapper.toResponseVO(saved);
    }

    public AddressResponseVO updateAddress(Long id, AddressRequestVO requestVO) {
        Map<String, String> validationErrors = validateAddressData(requestVO);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Dados de endereço inválidos: " + validationErrors);
        }
        Address current = addressDAO.findAddressById(id);
        addressMapper.toEntity(current, requestVO);
        Address updated = addressDAO.updateAddress(id, current);
        return addressMapper.toResponseVO(updated);
    }

    public AddressResponseVO findAddressById(Long id) {
        Address entity = addressDAO.findAddressById(id);
        return addressMapper.toResponseVO(entity);
    }

    // Porta de serviço para retornar entidade (sem VO) quando necessário em serviços de aplicação
    @Transactional(readOnly = true)
    public Address getEntityById(Long id) {
        return addressDAO.findAddressById(id);
    }

    public List<AddressResponseVO> findAllAddresses() {
        return addressDAO.findAllAddresses().stream()
                .map(addressMapper::toResponseVO)
                .collect(Collectors.toList());
    }

    public String deleteAddress(Long id) {
        return addressDAO.deleteAddress(id);
    }

    @Transactional
    public void deleteAddressesFromOtherUsers(Long adminId) {
        addressDAO.deleteAddressesFromOtherUsers(adminId);
    }

    // Método utilitário para uso interno em outros módulos: retorna Entidade
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