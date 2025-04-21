package com.devmaster.goatfarm.owner.api.controller;

import com.devmaster.goatfarm.owner.api.dto.OwnerRequestDTO;
import com.devmaster.goatfarm.owner.api.dto.OwnerResponseDTO;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.converter.OwnerDTOConverter;
import com.devmaster.goatfarm.owner.facade.OwnerFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<List<OwnerResponseDTO>> getAllOwners() {
        List<OwnerResponseDTO> responseDTOList = ownerFacade.findAllOwners().stream()
                .map(OwnerDTOConverter::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long id) {
        ownerFacade.deleteOwner(id);
        return ResponseEntity.noContent().build();
    }
}