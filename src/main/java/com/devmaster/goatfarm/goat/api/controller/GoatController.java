package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.converter.GoatFarmDTOConverter;
import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import com.devmaster.goatfarm.goat.facade.GoatFacade;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;  // Corrigido para OwnerResponseVO
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/goats")
public class GoatController {

    @Autowired
    private GoatFacade goatFacade;

    @GetMapping("/debug")
    public ResponseEntity<String> debugAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return ResponseEntity.status(403).body("Nenhuma autenticação ativa.");
        }

        return ResponseEntity.ok("Authorities reconhecidas: " + authentication.getAuthorities());
    }


    // CREATE
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<GoatResponseDTO> createGoat(@Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        Long farmId = goatRequestDTO.getFarmId(); // Extracts the farm ID from the DTO
        Long ownerId = goatRequestDTO.getOwnerId(); // Extracts the owner ID from the DTO

        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(goatFacade.createGoat(requestVO, ownerId, farmId)));
    }


    // UPDATE
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping(value = "/{registrationNumber}")
    public ResponseEntity<GoatResponseDTO> updateGoat(@PathVariable String registrationNumber,
                                                      @Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(goatFacade.updateGoat(registrationNumber, requestVO)));
    }

    @GetMapping(value = "/name")
    public ResponseEntity<Page<GoatResponseDTO>> searchGoatByName(@RequestParam(value = "name",
                                                                  defaultValue = "") String name,
                                                                  @PageableDefault(size = 12, page = 0)
                                                                  Pageable pageable) {

        return ResponseEntity.ok(goatFacade.searchGoatByName(name, pageable)
                .map(GoatDTOConverter::toResponseDTO));
    }

    // READ
    @GetMapping
    public ResponseEntity<Page<GoatResponseDTO>> findAllGoats(@PageableDefault(size = 12, page = 0)
                                                            Pageable pageable) {

        return ResponseEntity.ok(goatFacade.findAllGoats(pageable)
                .map(GoatDTOConverter::toResponseDTO));

    }

    @GetMapping("/{registrationNumber}")
    public ResponseEntity<GoatResponseDTO> findByRegistrationNumber(@PathVariable String registrationNumber) {
        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(
                goatFacade.findGoatByRegistrationNumber(registrationNumber)));
    }

    // DELETE
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{registrationNumber}")
    public ResponseEntity<Void> deleteGoat(@PathVariable String registrationNumber) {
        goatFacade.deleteGoatByRegistrationNumber(registrationNumber);
        return ResponseEntity.noContent().build();
    }

}