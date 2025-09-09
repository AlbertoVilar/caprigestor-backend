package com.devmaster.goatfarm.goat.facade;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GoatFacade {

    private final GoatBusiness goatBusiness;

    @Autowired
    public GoatFacade(GoatBusiness goatBusiness) {
        this.goatBusiness = goatBusiness;
    }

    /**
     * Cria uma nova cabra no sistema.
     * Delega a operação para a camada de negócio (GoatBusiness).
     * @param requestVO Objeto de requisição com os dados da cabra.
     * @param userId ID do usuário da cabra.
     * @param farmId ID da fazenda onde a cabra será cadastrada.
     * @return GoatResponseVO com os dados da cabra criada.
     */
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long userId, Long farmId) {
        return goatBusiness.createGoat(requestVO, userId, farmId);
    }

    /**
     * Busca uma cabra específica pelo seu número de registro.
     * Delega a operação para a camada de negócio.
     * @param registrationNumber Número de registro da cabra.
     * @return GoatResponseVO com os dados da cabra encontrada.
     */
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
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
     * Delega a operação para a camada de negócio.
     * @param farmId ID da fazenda onde a busca será realizada.
     * @param name Nome ou parte do nome da cabra a ser buscada.
     * @param pageable Objeto Pageable para controle de paginação.
     * @return Uma página de GoatResponseVOs que correspondem aos critérios de busca.
     */
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatBusiness.findGoatsByNameAndFarmId(farmId, name, pageable);
    }

    /**
     * Realiza uma busca paginada de cabras por ID da fazenda, com filtro opcional por número de registro.
     * Delega a operação para a camada de negócio.
     * @param farmId ID da fazenda.
     * @param registrationNumber Número de registro da cabra (opcional).
     * @param pageable Objeto Pageable para controle de paginação.
     * @return Uma página de GoatResponseVOs que correspondem aos critérios de busca.
     */
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId,
                                                                       String registrationNumber,
                                                                       Pageable pageable) {
        // Este método agora chama o Business, que por sua vez chama o DAO e o Repository com a Native Query corrigida.
        return goatBusiness.findGoatsByFarmIdAndRegistrationNumber(farmId, registrationNumber, pageable);
    }

    /**
     * Atualiza os dados de uma cabra existente.
     * Delega a operação para a camada de negócio.
     * @param numRegistration Número de registro da cabra a ser atualizada.
     * @param goatRequestVO Objeto de requisição com os novos dados da cabra.
     * @return GoatResponseVO com os dados da cabra atualizada.
     */
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO goatRequestVO) {
        return goatBusiness.updateGoat(numRegistration, goatRequestVO);
    }

    /**
     * Remove uma cabra do sistema pelo seu número de registro.
     * Delega a operação para a camada de negócio.
     * @param registrationNumber Número de registro da cabra a ser removida.
     */
    public void deleteGoatByRegistrationNumber(String registrationNumber) {
        goatBusiness.deleteGoat(registrationNumber);
    }


}