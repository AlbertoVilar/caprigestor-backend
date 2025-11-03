package com.devmaster.goatfarm.goat.facade;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoatFacade {

    private final GoatBusiness goatBusiness;
    private final OwnershipService ownershipService;
    private final GoatFarmDAO goatFarmDAO;
    private final GoatMapper goatMapper;

    @Autowired
    public GoatFacade(GoatBusiness goatBusiness, OwnershipService ownershipService, GoatFarmDAO goatFarmDAO, GoatMapper goatMapper) {
        this.goatBusiness = goatBusiness;
        this.ownershipService = ownershipService;
        this.goatFarmDAO = goatFarmDAO;
        this.goatMapper = goatMapper;
    }

    /**
     * Cria uma nova cabra no sistema.
     * Verifica a posse da fazenda antes de delegar para a camada de negÃ³cio.
     * @param requestVO Objeto de requisiÃ§Ã£o com os dados da cabra.
     * @param userId ID do usuÃ¡rio.
     * @param farmId ID da fazenda onde a cabra serÃ¡ registrada.
     * @return GoatResponseVO com os dados da cabra criada.
     */
    @Transactional
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long userId, Long farmId) {
        GoatFarm farm = goatFarmDAO.findFarmEntityById(farmId);
        ownershipService.verifyFarmOwnership(farm);
        return goatBusiness.createGoat(requestVO);
    }

    /**
     * Atualiza os dados de uma cabra existente.
     * Verifica a posse da cabra antes de delegar para a camada de negÃ³cio.
     * @param registrationNumber NÃºmero de registro da cabra a ser atualizada.
     * @param requestVO Objeto com os novos dados da cabra.
     * @return GoatResponseVO com os dados da cabra atualizada.
     */
    @Transactional
    public GoatResponseVO updateGoat(String registrationNumber, GoatRequestVO requestVO) {
        ownershipService.verifyOwnershipByGoatId(registrationNumber);
        return goatBusiness.updateGoat(registrationNumber, requestVO);
    }

    /**
     * Remove uma cabra do sistema pelo seu nÃºmero de registro.
     * Verifica a posse da cabra antes de delegar para a camada de negÃ³cio.
     * @param registrationNumber NÃºmero de registro da cabra a ser removida.
     */
    @Transactional
    public void deleteGoat(String registrationNumber) {
                ownershipService.verifyOwnershipByGoatId(registrationNumber);
        goatBusiness.deleteGoat(registrationNumber);
    }


            
    /**
     * Busca uma cabra especÃ­fica pelo seu nÃºmero de registro.
     * @param registrationNumber NÃºmero de registro da cabra.
     * @return GoatResponseVO com os dados da cabra encontrada.
     */
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatBusiness.findGoatByRegistrationNumber(registrationNumber);
    }

    /**
     * Retorna uma lista paginada de todas as cabras cadastradas.
     * @param pageable Objeto Pageable para controle de paginaÃ§Ã£o.
     * @return Uma pÃ¡gina de GoatResponseVOs.
     */
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatBusiness.findAllGoats(pageable);
    }

    /**
     * Realiza uma busca paginada de cabras por nome (sem filtro de fazenda).
     * @param name Nome ou parte do nome da cabra a ser buscada.
     * @param pageable Objeto Pageable para controle de paginaÃ§Ã£o.
     * @return Uma pÃ¡gina de GoatResponseVOs.
     */
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        return goatBusiness.searchGoatByName(name, pageable);
    }

    /**
     * Realiza uma busca paginada de cabras por nome dentro de uma fazenda especÃ­fica.
     * @param farmId ID da fazenda onde a busca serÃ¡ realizada.
     * @param name Nome ou parte do nome da cabra a ser buscada.
     * @param pageable Objeto Pageable para controle de paginaÃ§Ã£o.
     * @return Uma pÃ¡gina de GoatResponseVOs.
     */
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatBusiness.findGoatsByNameAndFarmId(farmId, name, pageable);
    }

    /**
     * Realiza uma busca paginada de cabras por ID da fazenda, com filtro opcional por nÃºmero de registro.
     * @param farmId ID da fazenda.
     * @param registrationNumber NÃºmero de registro da cabra (opcional).
     * @param pageable Objeto Pageable para controle de paginaÃ§Ã£o.
     * @return Uma pÃ¡gina de GoatResponseVOs.
     */
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId,
                                                                       String registrationNumber,
                                                                       Pageable pageable) {
        return goatBusiness.findGoatsByFarmIdAndRegistrationNumber(farmId, registrationNumber, pageable);
    }
}
