package com.devmaster.goatfarm.owner.api.controller;

import com.devmaster.goatfarm.owner.api.dto.OwnerRequestDTO;
import com.devmaster.goatfarm.owner.api.dto.OwnerResponseDTO;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.converter.OwnerDTOConverter;
import com.devmaster.goatfarm.owner.facade.OwnerFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/owners")
public class OwnerController {

    @Autowired
    private OwnerFacade ownerFacade;

    @PostMapping
    public ResponseEntity<OwnerResponseDTO> createOwner(@Valid @RequestBody OwnerRequestDTO requestDTO) {
        if (requestDTO == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        OwnerRequestVO requestVO = OwnerDTOConverter.toVO(requestDTO);
        OwnerResponseDTO responseDTO = OwnerDTOConverter.toDTO(ownerFacade.createOwner(requestVO));
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OwnerResponseDTO> updateOwner(@PathVariable Long id,
                                                        @Valid @RequestBody OwnerRequestDTO requestDTO) {
        if (requestDTO == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        OwnerRequestVO requestVO = OwnerDTOConverter.toVO(requestDTO);
        OwnerResponseDTO responseDTO = OwnerDTOConverter.toDTO(ownerFacade.updateGoatOwner(id, requestVO));
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OwnerResponseDTO> getOwnerById(@PathVariable Long id) {
        OwnerResponseDTO responseDTO = OwnerDTOConverter.toDTO(ownerFacade.findOwnerById(id));
        if (responseDTO != null) {
            return ResponseEntity.ok(responseDTO);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<Page<OwnerResponseDTO>> searchOwnersByName(
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        String trimmedName = name.trim(); // Remove espaços em branco iniciais e finais
        Page<OwnerResponseVO> responsePage = ownerFacade.searchOwnerByName(trimmedName, pageable);
        return ResponseEntity.ok(responsePage.map(OwnerDTOConverter::toDTO));
    }

   // @GetMapping
    public ResponseEntity<Page<OwnerResponseDTO>> getAllOwners(@PageableDefault(size = 12, page = 0) Pageable pageable) {
        Page<OwnerResponseDTO> responseDTOList = ownerFacade.findAllOwners(pageable)
                .map(OwnerDTOConverter::toDTO);

        return ResponseEntity.ok(responseDTOList);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long id) {
        ownerFacade.deleteOwner(id);
        return ResponseEntity.noContent().build();
    }
}