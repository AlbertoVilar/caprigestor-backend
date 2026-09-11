package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatHerdSummaryVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.application.pagination.GoatPage;
import com.devmaster.goatfarm.goat.application.pagination.GoatPageQuery;

import java.util.List;

/**
 * Porta de entrada (Use Case) para operações de gerenciamento de cabras.
 */
public interface GoatManagementUseCase {

    GoatResponseVO createGoat(Long farmId, GoatRequestVO requestVO);

    GoatResponseVO updateGoat(Long farmId, String goatId, GoatRequestVO requestVO);

    GoatExitResponseVO exitGoat(Long farmId, String goatId, GoatExitRequestVO requestVO);

    void deleteGoat(Long farmId, String goatId);

    GoatResponseVO findGoatById(Long farmId, String goatId);

    GoatPage<GoatResponseVO> findAllGoatsByFarm(Long farmId, GoatPageQuery query);

    GoatPage<GoatResponseVO> findAllGoatsByFarm(Long farmId, GoatBreed breed, GoatPageQuery query);

    GoatPage<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, GoatPageQuery query);

    GoatPage<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, GoatBreed breed, GoatPageQuery query);

    List<GoatResponseVO> listOffspring(Long farmId, String goatId);

    GoatHerdSummaryVO getGoatHerdSummary(Long farmId);
}
