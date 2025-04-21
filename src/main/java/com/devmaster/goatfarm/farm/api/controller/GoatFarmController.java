package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converter.GoatFarmDTOConverter;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/goatfarms")
public class GoatFarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

    @PostMapping
    public ResponseEntity<GoatFarmResponseDTO> createGoatFarm(@RequestBody GoatFarmRequestDTO requestDTO) {
        GoatFarmResponseVO responseVO = farmFacade.createGoatFarm(GoatFarmDTOConverter.toVO(requestDTO));
        return new ResponseEntity<>(GoatFarmDTOConverter.toDTO(responseVO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmResponseDTO> updateGoatFarm(@PathVariable Long id,
                                                              @RequestBody GoatFarmRequestDTO requestDTO) {
        GoatFarmResponseVO responseVO = farmFacade.updateGoatFarm(id, GoatFarmDTOConverter.toVO(requestDTO));
        return ResponseEntity.ok(GoatFarmDTOConverter.toDTO(responseVO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmResponseDTO> findGoatFarmById(@PathVariable Long id) {
        GoatFarmResponseVO responseVO = farmFacade.findGoatFarmById(id);
        return ResponseEntity.ok(GoatFarmDTOConverter.toDTO(responseVO));
    }

    @GetMapping
    public ResponseEntity<List<GoatFarmResponseDTO>> findAllGoatFarms() {
        return ResponseEntity.ok(farmFacade.findALLGoatFarm()
                .stream()
                .map(GoatFarmDTOConverter::toDTO)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(@PathVariable Long id) {
        farmFacade.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }
}