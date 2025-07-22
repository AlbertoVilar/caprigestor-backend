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

    // CREATE
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long ownerId, Long farmId) {
        return goatBusiness.createGoat(requestVO, ownerId, farmId);
    }

    // READ
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatBusiness.findGoatByRegistrationNumber(registrationNumber);
    }

    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatBusiness.findAllGoats(pageable);
    }

    // 🔍 Busca por nome (sem filtro de fazenda)
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        return goatBusiness.searchGoatByName(name, pageable);
    }

    // ✅ Ajustado: Busca por nome dentro de uma fazenda
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatBusiness.findGoatsByNameAndFarmId(farmId, name, pageable);
    }

    // 🔍 Busca por número de registro dentro de uma fazenda (opcional)
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId,
                                                                       String registrationNumber,
                                                                       Pageable pageable) {
        return goatBusiness.findGoatsByFarmIdAndRegistrationNumber(farmId, registrationNumber, pageable);
    }

    // UPDATE
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO goatRequestVO) {
        return goatBusiness.updateGoat(numRegistration, goatRequestVO);
    }

    // DELETE
    public void deleteGoatByRegistrationNumber(String registrationNumber) {
        goatBusiness.deleteGoat(registrationNumber);
    }
}
