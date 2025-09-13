package com.devmaster.goatfarm.goat.facade;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoatFacade {

    private final GoatBusiness goatBusiness;
    private final OwnershipService ownershipService;
    private final GoatFarmRepository goatFarmRepository;

    @Autowired
    public GoatFacade(GoatBusiness goatBusiness, OwnershipService ownershipService, GoatFarmRepository goatFarmRepository) {
        this.goatBusiness = goatBusiness;
        this.ownershipService = ownershipService;
        this.goatFarmRepository = goatFarmRepository;
    }

    /**
     * Creates a new goat in the system.
     * Verifies farm ownership before delegating to business layer.
     * @param requestVO Request object with goat data.
     * @param userId User ID of the goat.
     * @param farmId Farm ID where the goat will be registered.
     * @return GoatResponseVO with the created goat data.
     */
    @Transactional
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long userId, Long farmId) {
        // Verify farm ownership before creating the goat
        GoatFarm farm = goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Farm not found with ID: " + farmId));
        
        ownershipService.verifyFarmOwnership(farm);
        
        return goatBusiness.createGoat(requestVO, userId, farmId);
    }

    /**
     * Busca uma cabra específica pelo seu número de registro.
     * Verifica ownership antes de delegar para a camada de negócio.
     * @param registrationNumber Número de registro da cabra.
     * @return GoatResponseVO com os dados da cabra encontrada.
     */
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        // Verifica ownership da cabra antes de buscar
        ownershipService.verifyOwnershipByGoatId(registrationNumber);
        return goatBusiness.findGoatByRegistrationNumber(registrationNumber);
    }

    /**
     * Retorna uma lista paginada de todas as cabras cadastradas.
     * Delega a operação para a camada de negócio.
     * @param pageable Objeto Pageable para controle de paginação.
     * @return Uma página de GoatResponseVOs.
     */
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatBusiness.findAllGoats(pageable);
    }

    /**
     * Realiza uma busca paginada de cabras por nome (sem filtro de fazenda).
     * Delega a operação para a camada de negócio.
     * @param name Nome ou parte do nome da cabra a ser buscada.
     * @param pageable Objeto Pageable para controle de paginação.
     * @return Uma página de GoatResponseVOs que correspondem ao critério de busca.
     */
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        return goatBusiness.searchGoatByName(name, pageable);
    }

    /**
     * Realiza uma busca paginada de cabras por nome dentro de uma fazenda específica.
     * Verifica ownership da fazenda antes de delegar para a camada de negócio.
     * @param farmId ID da fazenda onde a busca será realizada.
     * @param name Nome ou parte do nome da cabra a ser buscada.
     * @param pageable Objeto Pageable para controle de paginação.
     * @return Uma página de GoatResponseVOs que correspondem aos critérios de busca.
     */
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        // Verifica ownership da fazenda antes de buscar
        ownershipService.verifyFarmOwnershipById(farmId);
        return goatBusiness.findGoatsByNameAndFarmId(farmId, name, pageable);
    }

    /**
     * Realiza uma busca paginada de cabras por ID da fazenda, com filtro opcional por número de registro.
     * Verifica ownership da fazenda antes de delegar para a camada de negócio.
     * @param farmId ID da fazenda.
     * @param registrationNumber Número de registro da cabra (opcional).
     * @param pageable Objeto Pageable para controle de paginação.
     * @return Uma página de GoatResponseVOs que correspondem aos critérios de busca.
     */
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId,
                                                                       String registrationNumber,
                                                                       Pageable pageable) {
        // Verifica ownership da fazenda antes de buscar
        ownershipService.verifyFarmOwnershipById(farmId);
        return goatBusiness.findGoatsByFarmIdAndRegistrationNumber(farmId, registrationNumber, pageable);
    }

    /**
     * Atualiza os dados de uma cabra existente.
     * Verifica ownership antes de delegar para a camada de negócio.
     * @param numRegistration Número de registro da cabra a ser atualizada.
     * @param goatRequestVO Objeto com os novos dados da cabra.
     * @return GoatResponseVO com os dados da cabra atualizada.
     */
    @Transactional
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO goatRequestVO) {
        // Verifica ownership da cabra antes de atualizar
        ownershipService.verifyOwnershipByGoatId(numRegistration);
        return goatBusiness.updateGoat(numRegistration, goatRequestVO);
    }

    /**
     * Remove uma cabra do sistema pelo seu número de registro.
     * Verifica ownership antes de delegar para a camada de negócio.
     * @param registrationNumber Número de registro da cabra a ser removida.
     */
    @Transactional
    public void deleteGoatByRegistrationNumber(String registrationNumber) {
        // Verifica ownership da cabra antes de remover
        ownershipService.verifyOwnershipByGoatId(registrationNumber);
        goatBusiness.deleteGoat(registrationNumber);
    }


}