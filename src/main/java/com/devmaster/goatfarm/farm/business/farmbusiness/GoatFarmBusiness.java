package com.devmaster.goatfarm.farm.business.farmbusiness;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatFarmBusiness {

    @Autowired
    private GoatFarmDAO goatFarmDAO;

    // ✅ Criação completa (fazenda + proprietário + endereço + telefones)
    public GoatFarmFullResponseVO createFullGoatFarm(GoatFarmRequestVO farmVO,
                                                     UserRequestVO userVO,
                                                     AddressRequestVO addressVO,
                                                     List<PhoneRequestVO> phoneVOs) {
        return goatFarmDAO.createFullGoatFarm(farmVO, userVO, addressVO, phoneVOs);
    }

    // Criação
    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {
        return goatFarmDAO.createGoatFarm(requestVO);
    }

    public GoatFarmFullResponseVO updateGoatFarm(Long id,
                                                 GoatFarmRequestVO requestVO,
                                                 UserRequestVO userVO,
                                                 AddressRequestVO addressVO,
                                                 List<PhoneRequestVO> phoneVOs) {
        return goatFarmDAO.updateGoatFarm(id, requestVO, userVO, addressVO, phoneVOs);
    }


    // Busca por ID
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        return goatFarmDAO.findGoatFarmById(id);
    }

    // Busca por nome
    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        return goatFarmDAO.searchGoatFarmByName(name, pageable);
    }

    // Listar todas as fazendas
    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        return goatFarmDAO.findAllGoatFarm(pageable);
    }

    // Deleção
    public String deleteGoatFarm(Long id) {
        return goatFarmDAO.deleteGoatFarm(id);
    }
}