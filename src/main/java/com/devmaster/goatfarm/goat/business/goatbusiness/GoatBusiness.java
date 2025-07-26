package com.devmaster.goatfarm.goat.business.goatbusiness;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GoatBusiness {

    private final GoatDAO goatDAO;

    @Autowired
    public GoatBusiness(GoatDAO goatDAO) {
        this.goatDAO = goatDAO;
    }

    /**
     * Cria uma nova cabra.
     * Delega a operação para a camada DAO.
     */
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long ownerId, Long farmId) {
        return goatDAO.createGoat(requestVO, ownerId, farmId);
    }

    /**
     * Busca uma cabra pelo número de registro.
     * Delega a operação para a camada DAO.
     */
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatDAO.findGoatById(registrationNumber);
    }

    /**
     * Lista todas as cabras paginadas.
     * Delega a operação para a camada DAO.
     */
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatDAO.findAllGoats(pageable);
    }

    /**
     * Busca paginada de cabras por nome (sem considerar fazenda).
     * Delega a operação para a camada DAO.
     */
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        return goatDAO.searchGoatByName(name, pageable);
    }

    /**
     * Busca paginada de cabras por nome dentro de uma fazenda específica.
     * Delega a operação para a camada DAO.
     */
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatDAO.searchGoatByNameAndFarmId(farmId, name, pageable);
    }

    /**
     * Busca paginada de cabras por ID da fazenda e número de registro (opcional).
     * Delega a operação para a camada DAO.
     */
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId,
                                                                       String registrationNumber,
                                                                       Pageable pageable) {
        // Este método agora chama o DAO, que por sua vez chama o Repository com a Native Query corrigida.
        return goatDAO.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);
    }

    /**
     * Atualiza os dados de uma cabra existente.
     * Delega a operação para a camada DAO.
     */
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO requestVO) {
        return goatDAO.updateGoat(numRegistration, requestVO);
    }

    /**
     * Remove uma cabra do sistema.
     * Delega a operação para a camada DAO.
     */
    public void deleteGoat(String registrationNumber) {
        goatDAO.deleteGoat(registrationNumber);
    }
}