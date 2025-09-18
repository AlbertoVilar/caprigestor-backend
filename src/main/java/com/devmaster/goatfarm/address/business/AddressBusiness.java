package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AddressBusiness {

    @Autowired
    private AddressDAO addressDAO;

    public AddressResponseVO createAddress(AddressRequestVO requestVO) {
        // Validações de negócio
        Map<String, String> validationErrors = validateAddressData(requestVO);
        
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Dados de endereço inválidos: " + validationErrors.toString());
        }
        
        return addressDAO.createAddress(requestVO);
    }

    public AddressResponseVO updateAddress(Long id, AddressRequestVO requestVO) {
        // Validações de negócio
        Map<String, String> validationErrors = validateAddressData(requestVO);
        
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Dados de endereço inválidos: " + validationErrors.toString());
        }
        
        return addressDAO.updateAddress(id, requestVO);
    }

    public AddressResponseVO findAddressById(Long id) {
        return addressDAO.findAddressById(id);
    }

    public List<AddressResponseVO> findAllAddresses() {
        return addressDAO.findAllAddresses();
    }

    public String deleteAddress(Long id) {
        return addressDAO.deleteAddress(id);
    }

    private Map<String, String> validateAddressData(AddressRequestVO requestVO) {
        Map<String, String> validationErrors = new HashMap<>();
        
        // Validar CEP brasileiro mais rigorosamente
        if (requestVO.getZipCode() != null) {
            String cep = requestVO.getZipCode().replaceAll("[^0-9]", "");
            if (!cep.matches("^\\d{8}$")) {
                validationErrors.put("zipCode", "CEP deve conter exatamente 8 dígitos numéricos");
            }
        }
        
        // Validar estado brasileiro (siglas ou nomes completos válidos)
        if (requestVO.getState() != null) {
            if (!isValidBrazilianState(requestVO.getState())) {
                validationErrors.put("state", "Estado deve ser uma sigla válida (ex: SP, RJ, MG) ou nome completo (ex: São Paulo, Rio de Janeiro, Minas Gerais)");
            }
        }
        
        // Validar país (aceitar apenas Brasil por enquanto)
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
        
        // Verificar siglas
        for (String sigla : siglasValidas) {
            if (sigla.equalsIgnoreCase(estadoInput)) {
                return true;
            }
        }
        
        // Se não encontrou pela sigla, verificar nomes completos
        for (String nome : nomesValidos) {
            if (nome.equalsIgnoreCase(estadoInput)) {
                return true;
            }
        }
        
        return false;
    }
}