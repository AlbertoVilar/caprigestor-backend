package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.application.ports.out.AddressPersistencePort;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressBusiness implements com.devmaster.goatfarm.application.ports.in.AddressManagementUseCase {

    @Autowired
    private AddressPersistencePort addressPort;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private OwnershipService ownershipService;

    public AddressResponseVO createAddress(Long farmId, AddressRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validateAddressData(requestVO);
        Address entity = addressMapper.toEntity(requestVO);
        Address saved = addressPort.save(entity);
        return addressMapper.toResponseVO(saved);
    }

    public AddressResponseVO updateAddress(Long farmId, Long addressId, AddressRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validateAddressData(requestVO);
        Address current = addressPort.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        addressMapper.toEntity(current, requestVO);
        Address updated = addressPort.save(current);
        return addressMapper.toResponseVO(updated);
    }

    public AddressResponseVO findAddressById(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        Address found = addressPort.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        return addressMapper.toResponseVO(found);
    }

    public String deleteAddress(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        // Garante que o endereço pertence à fazenda antes de deletar
        addressPort.findByIdAndFarmId(addressId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + addressId + " não encontrado na fazenda " + farmId));
        addressPort.deleteById(addressId);
        return "Endereço com ID " + addressId + " foi deletado com sucesso.";
    }


    private void validateAddressData(AddressRequestVO requestVO) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ValidationError validationError = new ValidationError(Instant.now(), 422, "Erro de validação", request.getRequestURI());

        if (requestVO.getZipCode() != null) {
            String cep = requestVO.getZipCode().replaceAll("[^0-9]", "");
            if (!cep.matches("^\\d{8}$")) {
                validationError.addError("zipCode", "CEP deve conter exatamente 8 dígitos numéricos");
            }
        }
        if (requestVO.getState() != null) {
            if (!isValidBrazilianState(requestVO.getState())) {
                validationError.addError("state", "Estado deve ser uma sigla válida (ex: SP, RJ, MG) ou nome completo (ex: São Paulo, Rio de Janeiro, Minas Gerais)");
            }
        }
        if (requestVO.getCountry() != null &&
                !requestVO.getCountry().trim().equalsIgnoreCase("Brasil") &&
                !requestVO.getCountry().trim().equalsIgnoreCase("Brazil")) {
            validationError.addError("country", "Por enquanto, apenas endereços do Brasil são aceitos");
        }

        if (!validationError.getErrors().isEmpty()) {
            throw new ValidationException(validationError);
        }
    }

    public Address findOrCreateAddressEntity(AddressRequestVO requestVO) {
        validateAddressData(requestVO);
        // Alteração crítica: Não buscamos mais endereço existente para reutilizar.
        // Como Address agora é dependente (orphanRemoval=true) da Farm, cada Farm deve ter sua própria instância de Address.
        // Isso evita que a deleção de uma fazenda apague o endereço compartilhado por outra.
        
        Address entity = addressMapper.toEntity(requestVO);
        return addressPort.save(entity);
    }

    private boolean isValidBrazilianState(String state) {
        if (state == null) return false;
        String raw = state.trim();
        if (raw.isEmpty()) return false;

        // Normaliza para comparar nomes sem acentos
        String normalized = java.text.Normalizer.normalize(raw, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String upper = raw.toUpperCase();
        String normalizedUpper = normalized.toUpperCase();

        // Siglas oficiais dos estados brasileiros
        java.util.Set<String> ufCodes = java.util.Set.of(
                "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS",
                "MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO"
        );

        // Nomes completos (sem acentos para comparação normalizada)
        java.util.Set<String> stateNamesNormalized = java.util.Set.of(
                "ACRE","ALAGOAS","AMAPA","AMAZONAS","BAHIA","CEARA","DISTRITO FEDERAL","ESPIRITO SANTO","GOIAS",
                "MARANHAO","MATO GROSSO","MATO GROSSO DO SUL","MINAS GERAIS","PARA","PARAIBA","PARANA","PERNAMBUCO",
                "PIAUI","RIO DE JANEIRO","RIO GRANDE DO NORTE","RIO GRANDE DO SUL","RONDONIA","RORAIMA","SANTA CATARINA",
                "SAO PAULO","SERGIPE","TOCANTINS"
        );

        return ufCodes.contains(upper) || stateNamesNormalized.contains(normalizedUpper);
    }
}