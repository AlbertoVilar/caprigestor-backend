package com.devmaster.goatfarm.farm.application.ports.in;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.farm.business.bo.FarmPermissionsVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

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

    PageResult<GoatFarmFullResponseVO> searchGoatFarmByName(String name, PageQuery pageQuery);

    PageResult<GoatFarmFullResponseVO> findAllGoatFarm(PageQuery pageQuery);

    void deleteGoatFarm(Long id);

    FarmPermissionsVO getFarmPermissions(Long farmId);
}
