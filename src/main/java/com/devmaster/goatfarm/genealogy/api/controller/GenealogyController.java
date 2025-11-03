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
import org.springframework.security.access.prepost.PreAuthorize;@RequestMapping("/api/genealogies")
public class GenealogyController {

    private final GenealogyFacade genealogyFacade;@Autowired
    private GenealogyMapper genealogyMapper;@Autowired
    public GenealogyController(GenealogyFacade genealogyFacade) {
        this.genealogyFacade = genealogyFacade;
    }@GetMapping("/{registrationNumber}")
    public ResponseEntity<GenealogyResponseDTO> getGenealogy(@PathVariable String registrationNumber) {
        GenealogyFacadeResponseDTO facadeDTO = genealogyFacade.findGenealogy(registrationNumber);
                GenealogyResponseVO responseVO = convertFacadeDTOToResponseVO(facadeDTO);
        GenealogyResponseDTO responseDTO = genealogyMapper.toResponseDTO(responseVO);
        return ResponseEntity.ok(responseDTO);
    }@PostMapping("/{registrationNumber}")@PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<GenealogyResponseDTO> createGenealogy(@PathVariable String registrationNumber) {
        GenealogyFacadeResponseDTO facadeDTO = genealogyFacade.createGenealogy(registrationNumber);
        GenealogyResponseVO createdResponseVO = convertFacadeDTOToResponseVO(facadeDTO);
        GenealogyResponseDTO createdResponseDTO = genealogyMapper.toResponseDTO(createdResponseVO);
        return new ResponseEntity<>(createdResponseDTO, HttpStatus.CREATED);
    }@PostMapping@PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
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
        
                responseVO.setBreeder(facadeDTO.getBreeder());
        responseVO.setFarmOwner(facadeDTO.getFarmOwner());
        responseVO.setColor(facadeDTO.getColor());
        responseVO.setStatus(facadeDTO.getStatus());
        responseVO.setCategory(facadeDTO.getCategory());
        responseVO.setTod(facadeDTO.getTod());
        
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
        
                responseVO.setPaternalGreatGrandfather1Name(facadeDTO.getPaternalGreatGrandfather1Name());
        responseVO.setPaternalGreatGrandfather1Registration(facadeDTO.getPaternalGreatGrandfather1Registration());
        responseVO.setPaternalGreatGrandmother1Name(facadeDTO.getPaternalGreatGrandmother1Name());
        responseVO.setPaternalGreatGrandmother1Registration(facadeDTO.getPaternalGreatGrandmother1Registration());
        responseVO.setPaternalGreatGrandfather2Name(facadeDTO.getPaternalGreatGrandfather2Name());
        responseVO.setPaternalGreatGrandfather2Registration(facadeDTO.getPaternalGreatGrandfather2Registration());
        responseVO.setPaternalGreatGrandmother2Name(facadeDTO.getPaternalGreatGrandmother2Name());
        responseVO.setPaternalGreatGrandmother2Registration(facadeDTO.getPaternalGreatGrandmother2Registration());
        
                responseVO.setMaternalGreatGrandfather1Name(facadeDTO.getMaternalGreatGrandfather1Name());
        responseVO.setMaternalGreatGrandfather1Registration(facadeDTO.getMaternalGreatGrandfather1Registration());
        responseVO.setMaternalGreatGrandmother1Name(facadeDTO.getMaternalGreatGrandmother1Name());
        responseVO.setMaternalGreatGrandmother1Registration(facadeDTO.getMaternalGreatGrandmother1Registration());
        responseVO.setMaternalGreatGrandfather2Name(facadeDTO.getMaternalGreatGrandfather2Name());
        responseVO.setMaternalGreatGrandfather2Registration(facadeDTO.getMaternalGreatGrandfather2Registration());
        responseVO.setMaternalGreatGrandmother2Name(facadeDTO.getMaternalGreatGrandmother2Name());
        responseVO.setMaternalGreatGrandmother2Registration(facadeDTO.getMaternalGreatGrandmother2Registration());
        
        return responseVO;
    }
}
