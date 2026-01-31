package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Porta de entrada (Use Case) para operações de gerenciamento de cabras.
 */
public interface GoatManagementUseCase {

    GoatResponseVO createGoat(Long farmId, GoatRequestVO requestVO);

    GoatResponseVO updateGoat(Long farmId, String goatId, GoatRequestVO requestVO);

    void deleteGoat(Long farmId, String goatId);

    GoatResponseVO findGoatById(Long farmId, String goatId);

    Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, Pageable pageable);

    Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, Pageable pageable);
}