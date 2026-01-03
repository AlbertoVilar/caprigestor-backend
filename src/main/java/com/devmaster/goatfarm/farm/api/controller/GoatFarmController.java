package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmUpdateRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.FarmPermissionsDTO;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/goatfarms")
public class GoatFarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

    @PostMapping
    public ResponseEntity<GoatFarmFullResponseDTO> createGoatFarm(@RequestBody @Valid GoatFarmFullRequestDTO requestDTO) {
        return new ResponseEntity<>(farmFacade.createGoatFarm(requestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> updateGoatFarm(@PathVariable Long id, @RequestBody @Valid GoatFarmUpdateRequestDTO requestDTO) {
        return ResponseEntity.ok(farmFacade.updateGoatFarm(id, requestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> findGoatFarmById(@PathVariable Long id) {
        return ResponseEntity.ok(farmFacade.findGoatFarmById(id));
    }

    @GetMapping("/name")
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> searchGoatFarmByName(
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable) {
        return ResponseEntity.ok(farmFacade.searchGoatFarmByName(name, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> findAllGoatFarm(@PageableDefault(size = 12, page = 0) Pageable pageable) {
        return ResponseEntity.ok(farmFacade.findAllGoatFarm(pageable));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(@PathVariable Long id) {
        farmFacade.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @GetMapping("/{farmId}/permissions")
    public ResponseEntity<FarmPermissionsDTO> getFarmPermissions(@PathVariable Long farmId) {
        return ResponseEntity.ok(farmFacade.getFarmPermissions(farmId));
    }
}
