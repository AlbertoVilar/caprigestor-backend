package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.application.ports.out.GenealogyPersistencePort;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyBusiness implements com.devmaster.goatfarm.application.ports.in.GenealogyManagementUseCase {

    private final GenealogyPersistencePort genealogyPort;
    private final GoatPersistencePort goatPort;
    private final GenealogyMapper genealogyMapper;
    private final OwnershipService ownershipService;

    @Autowired
    public GenealogyBusiness(GenealogyPersistencePort genealogyPort, GoatPersistencePort goatPort, GenealogyMapper genealogyMapper, OwnershipService ownershipService) {
        this.genealogyPort = genealogyPort;
        this.goatPort = goatPort;
        this.genealogyMapper = genealogyMapper;
        this.ownershipService = ownershipService;
    }

    @Transactional(readOnly = true)
    @Override
    public GenealogyResponseVO findGenealogy(Long farmId, String goatId) {
        return genealogyPort
                .findByGoatRegistrationAndGoatFarmId(goatId, farmId)
                .map(genealogyMapper::toResponseVO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Genealogia não encontrada para a cabra " + goatId + " na fazenda " + farmId));
    }

    @Transactional
    @Override
    public GenealogyResponseVO createGenealogy(Long farmId, String goatId) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        if (genealogyPort.existsByGoatRegistration(goatId)) {
            throw new DatabaseException("Genealogia já existe para o animal: " + goatId);
        }

        Goat goat = goatPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + goatId + " na fazenda " + farmId));

        final Genealogy entity = genealogyMapper.toEntity(goat);
        genealogyPort.save(entity);
        return genealogyMapper.toResponseVO(entity);
    }

    @Transactional
    @Override
    public GenealogyResponseVO createGenealogyWithData(Long farmId, String goatId, GenealogyRequestDTO requestDTO) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        if (genealogyPort.existsByGoatRegistration(requestDTO.getGoatRegistration())) {
            throw new DatabaseException("Genealogia já existe para o animal: " + requestDTO.getGoatRegistration());
        }
        // Verifica se o goatId da URL corresponde ao goatRegistration do DTO
        if (!goatId.equals(requestDTO.getGoatRegistration())) {
            throw new IllegalArgumentException("O ID da cabra na URL não corresponde ao ID da cabra no corpo da requisição.");
        }

        Goat goat = goatPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + goatId + " na fazenda " + farmId));

        // Se foram informados registros de pai/mãe no DTO, garantimos que o Goat em memória
        // tenha as referências corretas para que o mapper construa toda a árvore de ancestrais.
        try {
            if (requestDTO.getFatherRegistration() != null && !requestDTO.getFatherRegistration().isBlank()) {
                goatPort.findByRegistrationNumber(requestDTO.getFatherRegistration())
                        .ifPresent(goat::setFather);
            }
            if (requestDTO.getMotherRegistration() != null && !requestDTO.getMotherRegistration().isBlank()) {
                goatPort.findByRegistrationNumber(requestDTO.getMotherRegistration())
                        .ifPresent(goat::setMother);
            }
        } catch (Exception e) {
            throw new DatabaseException("Erro ao localizar parentes informados: " + e.getMessage());
        }

        // Monta a Genealogy completamente a partir do Goat (com pai/mãe preenchidos),
        // permitindo que o mapper derive avós e bisavós automaticamente.
        Genealogy entity = genealogyMapper.toEntity(goat);
        // Garante que a genealogia está associada à cabra correta
        entity.setGoatRegistration(goat.getRegistrationNumber());

        try {
            entity = genealogyPort.save(entity);
        } catch (Exception e) {
            throw new DatabaseException("Erro ao persistir genealogia: " + e.getMessage());
        }
        return genealogyMapper.toResponseVO(entity);
    }
}


