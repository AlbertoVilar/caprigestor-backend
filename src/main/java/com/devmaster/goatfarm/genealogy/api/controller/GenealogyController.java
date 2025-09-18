package com.devmaster.goatfarm.genealogy.api.controller;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import com.devmaster.goatfarm.genealogy.facade.GenealogyFacade;
import com.devmaster.goatfarm.genealogy.facade.dto.GenealogyFacadeResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/api/genealogies")
public class GenealogyController {

    private final GenealogyFacade genealogyFacade;
    // CORREÇÃO: GenealogyDTOConverter é uma classe utilitária com métodos estáticos, não precisa ser injetada

    @Autowired
    private GenealogyMapper genealogyMapper;

    @Autowired
    public GenealogyController(GenealogyFacade genealogyFacade) {
        this.genealogyFacade = genealogyFacade;
    }

    @GetMapping("/{registrationNumber}")
    public ResponseEntity<GenealogyResponseDTO> getGenealogy(@PathVariable String registrationNumber) {
        GenealogyFacadeResponseDTO facadeDTO = genealogyFacade.findGenealogy(registrationNumber);
        if (facadeDTO != null) {
            // Converter FacadeDTO para ResponseVO para manter compatibilidade
            GenealogyResponseVO responseVO = convertFacadeDTOToResponseVO(facadeDTO);
            GenealogyResponseDTO responseDTO = genealogyMapper.toResponseDTO(responseVO);
            return ResponseEntity.ok(responseDTO);
        } else {
          return ResponseEntity.notFound().build();
       }
    }

    @PostMapping("/{registrationNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FARM_OWNER') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogy(@PathVariable String registrationNumber) {
        GenealogyFacadeResponseDTO facadeDTO = genealogyFacade.createGenealogy(registrationNumber);
        GenealogyResponseVO createdResponseVO = convertFacadeDTOToResponseVO(facadeDTO);
        GenealogyResponseDTO createdResponseDTO = genealogyMapper.toResponseDTO(createdResponseVO);
        return new ResponseEntity<>(createdResponseDTO, HttpStatus.CREATED);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FARM_OWNER') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogyWithData(@RequestBody GenealogyRequestDTO requestDTO) {
        GenealogyFacadeResponseDTO facadeDTO = genealogyFacade.createGenealogyWithData(requestDTO);
        GenealogyResponseVO createdResponseVO = convertFacadeDTOToResponseVO(facadeDTO);
        GenealogyResponseDTO createdResponseDTO = genealogyMapper.toResponseDTO(createdResponseVO);
        return new ResponseEntity<>(createdResponseDTO, HttpStatus.CREATED);
    }

    private GenealogyResponseVO convertFacadeDTOToResponseVO(GenealogyFacadeResponseDTO facadeDTO) {
        GenealogyResponseVO responseVO = new GenealogyResponseVO();
        responseVO.setGoatName(facadeDTO.getGoatName());
        responseVO.setGoatRegistration(facadeDTO.getGoatRegistration());
        responseVO.setBreed(facadeDTO.getBreed());
        responseVO.setGender(facadeDTO.getGender());
        responseVO.setToe(facadeDTO.getToe());
        responseVO.setBirthDate(facadeDTO.getBirthDate());
        responseVO.setFatherName(facadeDTO.getFatherName());
        responseVO.setFatherRegistration(facadeDTO.getFatherRegistration());
        responseVO.setMotherName(facadeDTO.getMotherName());
        responseVO.setMotherRegistration(facadeDTO.getMotherRegistration());
        responseVO.setPaternalGrandfatherName(facadeDTO.getPaternalGrandfatherName());
        responseVO.setPaternalGrandfatherRegistration(facadeDTO.getPaternalGrandfatherRegistration());
        responseVO.setPaternalGrandmotherName(facadeDTO.getPaternalGrandmotherName());
        responseVO.setPaternalGrandmotherRegistration(facadeDTO.getPaternalGrandmotherRegistration());
        responseVO.setMaternalGrandfatherName(facadeDTO.getMaternalGrandfatherName());
        responseVO.setMaternalGrandfatherRegistration(facadeDTO.getMaternalGrandfatherRegistration());
        responseVO.setMaternalGrandmotherName(facadeDTO.getMaternalGrandmotherName());
        responseVO.setMaternalGrandmotherRegistration(facadeDTO.getMaternalGrandmotherRegistration());
        return responseVO;
    }
}