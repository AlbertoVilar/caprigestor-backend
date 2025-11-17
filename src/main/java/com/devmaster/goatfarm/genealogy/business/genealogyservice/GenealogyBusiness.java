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

    @Autowired
    private GenealogyPersistencePort genealogyPort;

    @Autowired
    private GoatPersistencePort goatPort;

    @Autowired
    private GenealogyMapper genealogyMapper;

    @Autowired
    private OwnershipService ownershipService;

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

        Goat goat = goatPort.findByIdAndFarmId(goatId, farmId)
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

        Goat goat = goatPort.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + goatId + " na fazenda " + farmId));

        Genealogy entity = genealogyMapper.toEntity(requestDTO);
        // Garante que a genealogia está associada à cabra correta
        entity.setGoatRegistration(goat.getRegistrationNumber());
        // Se o mapper não preencher o objeto Goat na Genealogy, pode ser necessário setar manualmente
        // entity.setGoat(goat);

        try {
            entity = genealogyPort.save(entity);
        } catch (Exception e) {
            throw new DatabaseException("Erro ao persistir genealogia: " + e.getMessage());
        }
        return genealogyMapper.toResponseVO(entity);
    }
}
