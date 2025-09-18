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
     * Cria uma fazenda completa com proprietário, endereço e telefones.
     * @param farmVO Dados da fazenda
     * @param userVO Dados do proprietário
     * @param addressVO Dados do endereço
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
     * Verifica ownership antes de delegar para a camada de negócio.
     * @param id ID da fazenda
     * @param requestVO Novos dados da fazenda
     * @param userVO Novos dados do proprietário
     * @param addressVO Novos dados do endereço
     * @param phoneVOs Nova lista de telefones
     * @return GoatFarmFullResponseVO com os dados atualizados
     */
    @Transactional
    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO requestVO,
                                                 UserRequestVO userVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {
        // Verifica ownership da fazenda antes de atualizar
        ownershipService.verifyFarmOwnershipById(id);
        return farmBusiness.updateGoatFarm(id, requestVO, userVO, addressVO, phoneVOs);
    }



    /**
     * Busca uma fazenda pelo ID.
     * Endpoint público - não requer autenticação.
     * @param id ID da fazenda
     * @return GoatFarmFullResponseVO com os dados da fazenda
     */
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        // Endpoint público - busca direta sem verificação de ownership
        return farmBusiness.findGoatFarmById(id);
    }

    /**
     * Busca fazendas por nome com paginação.
     * @param name Nome ou parte do nome da fazenda
     * @param pageable Configuração de paginação
     * @return Page de GoatFarmFullResponseVO
     */
    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        return farmBusiness.searchGoatFarmByName(name, pageable);
    }

    /**
     * Lista todas as fazendas com paginação.
     * @param pageable Configuração de paginação
     * @return Page de GoatFarmFullResponseVO
     */
    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        return farmBusiness.findAllGoatFarm(pageable);
    }

    /**
     * Remove uma fazenda pelo ID.
     * Verifica ownership antes de delegar para a camada de negócio.
     * @param id ID da fazenda a ser removida
     * @return String com mensagem de confirmação
     */
    @Transactional
    public String deleteGoatFarm(Long id) {
        // Verifica ownership da fazenda antes de remover
        ownershipService.verifyFarmOwnershipById(id);
        return farmBusiness.deleteGoatFarm(id);
    }

}
