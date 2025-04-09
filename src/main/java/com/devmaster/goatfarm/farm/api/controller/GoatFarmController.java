package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converter.GoatFarmDTOConverter;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/goatfarms")
public class GoatFarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

    @PostMapping
    public GoatFarmResponseDTO createGoatFarm(@RequestBody GoatFarmRequestDTO requestDTO) {

          GoatFarmResponseVO responseVO = farmFacade.createGoatFarm(GoatFarmDTOConverter.toVO(requestDTO));
        return GoatFarmDTOConverter.toDTO(responseVO);
    }

    @PutMapping("/{id}")
    public GoatFarmResponseDTO updateGoatFarm(@PathVariable Long id,
                                              @RequestBody GoatFarmRequestDTO requestDTO) {

        GoatFarmResponseVO responseVO = farmFacade.updateGoatFarm(id, GoatFarmDTOConverter.toVO(requestDTO));
        return GoatFarmDTOConverter.toDTO(responseVO);
    }

    @GetMapping("/{id}")
    public GoatFarmResponseDTO findGoatFarmById(@PathVariable Long id) {
        GoatFarmResponseVO responseVO = farmFacade.findGoatFarmById(id);
        return GoatFarmDTOConverter.toDTO(responseVO);
    }

    @GetMapping
    public List<GoatFarmResponseDTO> findAllGoatFarms() {
        return farmFacade.findALLGoatFarm()
                .stream()
                .map(GoatFarmDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public String deleteGoatFarm(@PathVariable Long id) {
        return farmFacade.deleteGoatFarm(id);
    }


}
