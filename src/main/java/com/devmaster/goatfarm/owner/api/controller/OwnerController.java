package com.devmaster.goatfarm.owner.api.controller;

import com.devmaster.goatfarm.owner.api.dto.OwnerRequestDTO;
import com.devmaster.goatfarm.owner.api.dto.OwnerResponseDTO;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.converter.OwnerDTOConverter;
import com.devmaster.goatfarm.owner.facade.OwnerFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owners")
public class OwnerController {

    @Autowired
    private OwnerFacade ownerFacade;
    @CrossOrigin(origins = "http://localhost:5500")
    @Operation(summary = "Cadastra um novo proprietário")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<OwnerResponseDTO> createOwner(
            @RequestBody(description = "Dados do novo proprietário")
            @org.springframework.web.bind.annotation.RequestBody @Valid OwnerRequestDTO requestDTO) {

        OwnerRequestVO requestVO = OwnerDTOConverter.toVO(requestDTO);
        OwnerResponseDTO responseDTO = OwnerDTOConverter.toDTO(ownerFacade.createOwner(requestVO));
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza um proprietário existente")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<OwnerResponseDTO> updateOwner(
            @Parameter(description = "ID do proprietário a ser atualizado", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados do proprietário")
            @org.springframework.web.bind.annotation.RequestBody @Valid OwnerRequestDTO requestDTO) {

        OwnerRequestVO requestVO = OwnerDTOConverter.toVO(requestDTO);
        OwnerResponseDTO responseDTO = OwnerDTOConverter.toDTO(ownerFacade.updateGoatOwner(id, requestVO));
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Busca um proprietário pelo ID")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OwnerResponseDTO> getOwnerById(
            @Parameter(description = "ID do proprietário", example = "1") @PathVariable Long id) {

        OwnerResponseDTO responseDTO = OwnerDTOConverter.toDTO(ownerFacade.findOwnerById(id));
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Busca paginada de proprietários pelo nome")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<OwnerResponseDTO>> searchOwnersByName(
            @Parameter(description = "Nome ou parte do nome do proprietário", example = "João")
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        String trimmedName = name.trim();
        Page<OwnerResponseVO> responsePage = ownerFacade.searchOwnerByName(trimmedName, pageable);
        return ResponseEntity.ok(responsePage.map(OwnerDTOConverter::toDTO));
    }

    @Operation(summary = "Lista todos os proprietários cadastrados (desativado)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    // @GetMapping
    public ResponseEntity<Page<OwnerResponseDTO>> getAllOwners(
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        Page<OwnerResponseDTO> responseDTOList = ownerFacade.findAllOwners(pageable)
                .map(OwnerDTOConverter::toDTO);
        return ResponseEntity.ok(responseDTOList);
    }

    @Operation(summary = "Remove um proprietário por ID")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOwner(
            @Parameter(description = "ID do proprietário a ser removido", example = "1")
            @PathVariable Long id) {

        ownerFacade.deleteOwner(id);
        return ResponseEntity.noContent().build();
    }
}
