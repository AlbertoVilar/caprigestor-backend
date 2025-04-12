package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.converter.GoatFarmDTOConverter;
import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import com.devmaster.goatfarm.goat.facade.GoatFacade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/goats")
public class GoatController {

    @Autowired
    private GoatFacade goatFacade;

    @PostMapping
    public GoatResponseDTO createGoat(@RequestBody GoatRequestDTO goatRequestDTO) {
        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        GoatFarmRequestVO farmRequest = extractFarmRequestVO(goatRequestDTO);
        return GoatDTOConverter.toResponseDTO(goatFacade.createGoat(requestVO, farmRequest));
    }

    @PutMapping
    public GoatResponseDTO updateGoat(@RequestBody GoatRequestDTO goatRequestDTO) {
        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        return GoatDTOConverter.toResponseDTO(goatFacade.updateGoat(requestVO));
    }

    @GetMapping
    public List<GoatResponseDTO> findAllGoats() {
        return goatFacade.findAllGoats().stream()
                .map(GoatDTOConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{registrationNumber}")
    public GoatResponseDTO findByRegistrationNumber(@PathVariable String registrationNumber) {
        return GoatDTOConverter.toResponseDTO(goatFacade.findGoatByRegistrationNumber(registrationNumber));
    }

    @DeleteMapping("/{registrationNumber}")
    public void deleteGoat(@PathVariable String registrationNumber) {
        goatFacade.deleteGoatByRegistrationNumber(registrationNumber);
    }

    // Método utilitário privado para extrair o VO da fazenda a partir do GoatRequestDTO
    private GoatFarmRequestVO extractFarmRequestVO(GoatRequestDTO goatRequestDTO) {
        GoatFarmRequestDTO farmRequestDTO = GoatFarmDTOConverter.fromGoatRequestDTO(goatRequestDTO);
        return GoatFarmDTOConverter.toVO(farmRequestDTO);
    }
}
