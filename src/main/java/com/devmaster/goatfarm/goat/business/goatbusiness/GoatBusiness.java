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

    @Autowired
    private GoatDAO goatDAO;

    // CREATE
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long ownerId, Long farmId) {
        return goatDAO.createGoat(requestVO, ownerId, farmId);
    }

    // READ (BY ID)
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatDAO.findGoatById(registrationNumber);
    }

    // READ (ALL)
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatDAO.findAllGoats(pageable);
    }

    // 🔍 Busca por nome (sem considerar fazenda)
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        return goatDAO.searchGoatByName(name, pageable);
    }

    // ✅ NOVO: Busca por nome dentro de uma fazenda (ajuste para bater com o Controller)
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatDAO.searchGoatByNameAndFarmId(farmId, name, pageable);
    }

    // 🔍 Busca por ID da fazenda e número de registro (opcional)
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId,
                                                                       String registrationNumber,
                                                                       Pageable pageable) {
        return goatDAO.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);
    }

    // UPDATE
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO requestVO) {
        return goatDAO.updateGoat(numRegistration, requestVO);
    }

    // DELETE
    public void deleteGoat(String registrationNumber) {
        goatDAO.deleteGoat(registrationNumber);
    }
}
