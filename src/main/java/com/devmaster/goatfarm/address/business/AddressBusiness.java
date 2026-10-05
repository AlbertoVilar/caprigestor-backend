package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.application.ports.out.AddressPersistencePort;
import com.devmaster.goatfarm.address.application.ports.in.AddressManagementUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class AddressBusiness implements AddressManagementUseCase {

    private final AddressPersistencePort addressPort;
    private final FarmAuthorizationUseCase ownershipService;
    private final EntityFinder entityFinder;

    public AddressBusiness(AddressPersistencePort addressPort, FarmAuthorizationUseCase ownershipService, EntityFinder entityFinder) {
        this.addressPort = addressPort;
        this.ownershipService = ownershipService;
        this.entityFinder = entityFinder;
    }

    public AddressResponseVO createAddress(Long farmId, AddressRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validateAddressData(requestVO);
        return addressPort.save(toResponse(requestVO));
    }

    public AddressResponseVO updateAddress(Long farmId, Long addressId, AddressRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validateAddressData(requestVO);
        AddressResponseVO current = entityFinder.findOrThrow(
                () -> addressPort.findByIdAndFarmId(addressId, farmId),
                "Endereço com ID " + addressId + " não encontrado na fazenda " + farmId
        );
        copy(requestVO, current);
        return addressPort.save(current);
    }

    public AddressResponseVO findAddressById(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        AddressResponseVO found = entityFinder.findOrThrow(
                () -> addressPort.findByIdAndFarmId(addressId, farmId),
                "Endereço com ID " + addressId + " não encontrado na fazenda " + farmId
        );
        return found;
    }

    public String deleteAddress(Long farmId, Long addressId) {
        ownershipService.verifyFarmOwnership(farmId);
        // Garante que o endereço pertence à fazenda antes de deletar
        entityFinder.findOrThrow(
                () -> addressPort.findByIdAndFarmId(addressId, farmId),
                "Endereço com ID " + addressId + " não encontrado na fazenda " + farmId
        );
        addressPort.deleteById(addressId);
        return "Endereço com ID " + addressId + " foi deletado com sucesso.";
    }


    private void validateAddressData(AddressRequestVO requestVO) {
        if (requestVO.getZipCode() != null) {
            String cep = requestVO.getZipCode().replaceAll("[^0-9]", "");
            if (!cep.matches("^\\d{8}$")) {
                throw new BusinessRuleException("zipCode", "CEP deve conter exatamente 8 dígitos numéricos");
            }
        }
        if (requestVO.getState() != null) {
            if (!isValidBrazilianState(requestVO.getState())) {
                throw new BusinessRuleException("state", "Estado deve ser uma sigla válida (ex: SP, RJ, MG) ou nome completo (ex: São Paulo, Rio de Janeiro, Minas Gerais)");
            }
        }
        if (requestVO.getCountry() != null &&
                !requestVO.getCountry().trim().equalsIgnoreCase("Brasil") &&
                !requestVO.getCountry().trim().equalsIgnoreCase("Brazil")) {
            throw new BusinessRuleException("country", "Por enquanto, apenas endereços do Brasil são aceitos");
        }
    }

    public AddressResponseVO findOrCreateAddress(AddressRequestVO requestVO) {
        validateAddressData(requestVO);
        // Alteração crítica: Não buscamos mais endereço existente para reutilizar.
        // Como Address agora é dependente (orphanRemoval=true) da Farm, cada Farm deve ter sua própria instância de Address.
        // Isso evita que a deleção de uma fazenda apague o endereço compartilhado por outra.
        
        return addressPort.save(toResponse(requestVO));
    }

    private AddressResponseVO toResponse(AddressRequestVO request) {
        return new AddressResponseVO(request.getId(), request.getStreet(), request.getCity(),
                request.getNeighborhood(), request.getState(), request.getZipCode(), request.getCountry());
    }

    private void copy(AddressRequestVO source, AddressResponseVO target) {
        target.setStreet(source.getStreet()); target.setCity(source.getCity());
        target.setNeighborhood(source.getNeighborhood()); target.setState(source.getState());
        target.setZipCode(source.getZipCode()); target.setCountry(source.getCountry());
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
        Set<String> stateNamesNormalized = java.util.Set.of(
                "ACRE","ALAGOAS","AMAPA","AMAZONAS","BAHIA","CEARA","DISTRITO FEDERAL","ESPIRITO SANTO","GOIAS",
                "MARANHAO","MATO GROSSO","MATO GROSSO DO SUL","MINAS GERAIS","PARA","PARAIBA","PARANA","PERNAMBUCO",
                "PIAUI","RIO DE JANEIRO","RIO GRANDE DO NORTE","RIO GRANDE DO SUL","RONDONIA","RORAIMA","SANTA CATARINA",
                "SAO PAULO","SERGIPE","TOCANTINS"
        );

        return ufCodes.contains(upper) || stateNamesNormalized.contains(normalizedUpper);
    }
}
