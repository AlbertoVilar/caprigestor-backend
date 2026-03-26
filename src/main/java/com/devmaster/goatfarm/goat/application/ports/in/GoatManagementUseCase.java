package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatHerdSummaryVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, Pageable pageable);

    Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, GoatBreed breed, Pageable pageable);

    Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, Pageable pageable);

    Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, GoatBreed breed, Pageable pageable);

    List<GoatResponseVO> listOffspring(Long farmId, String goatId);

    GoatHerdSummaryVO getGoatHerdSummary(Long farmId);
}
