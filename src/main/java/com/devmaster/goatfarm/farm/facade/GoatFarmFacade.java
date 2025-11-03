package com.devmaster.goatfarm.farm.facade;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.business.farmbusiness.GoatFarmBusiness;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoatFarmFacade {

    @Autowired
    private GoatFarmBusiness farmBusiness;
    
    @Autowired
    private OwnershipService ownershipService;

    /**
     * Cria uma fazenda completa com proprietÃ¡rio, endereÃ§o e telefones.
     * @param farmVO Dados da fazenda
     * @param userVO Dados do proprietÃ¡rio
     * @param addressVO Dados do endereÃ§o
     * @param phoneVOs Lista de telefones
     * @return GoatFarmFullResponseVO com os dados da fazenda criada
     */
    @Transactional
    public GoatFarmFullResponseVO createFullGoatFarm(GoatFarmRequestVO farmVO,
                                                     UserRequestVO userVO,
                                                     AddressRequestVO addressVO,
                                                     List<PhoneRequestVO> phoneVOs) {
        return farmBusiness.createFullGoatFarm(farmVO, userVO, addressVO, phoneVOs);
    }

    /**
     * Cria uma fazenda usando IDs de entidades existentes.
     * @param requestVO Dados da fazenda
     * @return GoatFarmResponseVO com os dados da fazenda criada
     */
    @Transactional
    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {
        return farmBusiness.createGoatFarm(requestVO);
    }

    /**
     * Atualiza uma fazenda existente.
     * Verifica ownership antes de delegar para a camada de negÃ³cio.
     * @param id ID da fazenda
     * @param requestVO Novos dados da fazenda
     * @param userVO Novos dados do proprietÃ¡rio
     * @param addressVO Novos dados do endereÃ§o
     * @param phoneVOs Nova lista de telefones
     * @return GoatFarmFullResponseVO com os dados atualizados
     */
    @Transactional
    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO requestVO,
                                                 UserRequestVO userVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {
                ownershipService.verifyFarmOwnershipById(id);
        return farmBusiness.updateGoatFarm(id, requestVO, userVO, addressVO, phoneVOs);
    }



    /**
     * Busca uma fazenda pelo ID.
     * Endpoint pÃºblico - nÃ£o requer autenticaÃ§Ã£o.
     * @param id ID da fazenda
     * @return GoatFarmFullResponseVO com os dados da fazenda
     */
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
                return farmBusiness.findGoatFarmById(id);
    }

    /**
     * Busca fazendas por nome com paginaÃ§Ã£o.
     * @param name Nome ou parte do nome da fazenda
     * @param pageable ConfiguraÃ§Ã£o de paginaÃ§Ã£o
     * @return Page de GoatFarmFullResponseVO
     */
    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        return farmBusiness.searchGoatFarmByName(name, pageable);
    }

    /**
     * Lista todas as fazendas com paginaÃ§Ã£o.
     * @param pageable ConfiguraÃ§Ã£o de paginaÃ§Ã£o
     * @return Page de GoatFarmFullResponseVO
     */
    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        return farmBusiness.findAllGoatFarm(pageable);
    }

    /**
     * Remove uma fazenda pelo ID.
     * Verifica ownership antes de delegar para a camada de negÃ³cio.
     * @param id ID da fazenda a ser removida
     * @return String com mensagem de confirmaÃ§Ã£o
     */
    @Transactional
    public String deleteGoatFarm(Long id) {
                ownershipService.verifyFarmOwnershipById(id);
        return farmBusiness.deleteGoatFarm(id);
    }

}

