package com.devmaster.goatfarm.goat.facade;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GoatFacade {

    private final GoatBusiness goatBusiness;
    private final GoatMapper goatMapper;

    @Autowired
    public GoatFacade(GoatBusiness goatBusiness, GoatMapper goatMapper) {
        this.goatBusiness = goatBusiness;
        this.goatMapper = goatMapper;
    }

    public GoatResponseDTO createGoat(Long farmId, GoatRequestDTO requestDTO) {
        return goatMapper.toResponseDTO(goatBusiness.createGoat(farmId, goatMapper.toRequestVO(requestDTO)));
    }

    public GoatResponseDTO updateGoat(Long farmId, String goatId, GoatRequestDTO requestDTO) {
        return goatMapper.toResponseDTO(goatBusiness.updateGoat(farmId, goatId, goatMapper.toRequestVO(requestDTO)));
    }

    public void deleteGoat(Long farmId, String goatId) {
        goatBusiness.deleteGoat(farmId, goatId);
    }

    public GoatResponseDTO findGoatById(Long farmId, String goatId) {
        return goatMapper.toResponseDTO(goatBusiness.findGoatById(farmId, goatId));
    }

    public Page<GoatResponseDTO> findAllGoatsByFarm(Long farmId, Pageable pageable) {
        return goatBusiness.findAllGoatsByFarm(farmId, pageable).map(goatMapper::toResponseDTO);
    }

    public Page<GoatResponseDTO> findGoatsByNameAndFarm(Long farmId, String name, Pageable pageable) {
        return goatBusiness.findGoatsByNameAndFarm(farmId, name, pageable).map(goatMapper::toResponseDTO);
    }
}
