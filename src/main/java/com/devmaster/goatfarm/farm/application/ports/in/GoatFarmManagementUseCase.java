package com.devmaster.goatfarm.farm.application.ports.in;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.farm.business.bo.FarmPermissionsVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Porta de entrada (Use Case) para operações de gerenciamento da Fazenda (GoatFarm).
 */
public interface GoatFarmManagementUseCase {

    GoatFarmFullResponseVO createGoatFarm(GoatFarmFullRequestVO fullRequestVO);

    GoatFarmFullResponseVO updateGoatFarm(Long id,
                                          GoatFarmRequestVO farmVO,
                                          UserRequestVO userVO,
                                          AddressRequestVO addressVO,
                                          List<PhoneRequestVO> phoneVOs);

    GoatFarmFullResponseVO findGoatFarmById(Long id);

    Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable);

    Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable);

    void deleteGoatFarm(Long id);

    FarmPermissionsVO getFarmPermissions(Long farmId);
}