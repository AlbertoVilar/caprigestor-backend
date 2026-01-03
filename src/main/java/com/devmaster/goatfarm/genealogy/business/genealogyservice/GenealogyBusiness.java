package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import com.devmaster.goatfarm.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de negócio para Genealogia.
 * <p>
 * ATENÇÃO: Mudança Arquitetural
 * O módulo de genealogia foi refatorado para funcionar como uma PROJEÇÃO SOB DEMANDA (Read Model).
 * Não há mais persistência de entidade 'Genealogy'. A fonte da verdade é o agregado 'Goat'
 * e seus relacionamentos (pai/mãe).
 * </p>
 */
@Service
public class GenealogyBusiness implements com.devmaster.goatfarm.application.ports.in.GenealogyManagementUseCase {

    private final GoatPersistencePort goatPort;
    private final GenealogyMapper genealogyMapper;
    private final OwnershipService ownershipService;

    @Autowired
    public GenealogyBusiness(GoatPersistencePort goatPort, GenealogyMapper genealogyMapper, OwnershipService ownershipService) {
        this.goatPort = goatPort;
        this.genealogyMapper = genealogyMapper;
        this.ownershipService = ownershipService;
    }

    /**
     * Busca a genealogia de uma cabra.
     * Agora opera como uma projeção (Query Use Case), montando a árvore genealógica
     * a partir dos relacionamentos da entidade Goat.
     */
    @Transactional(readOnly = true)
    @Override
    public GenealogyResponseVO findGenealogy(Long farmId, String goatId) {
        // Validação de ownership já implícita na busca por farmId, mas mantemos consistência
        // ownershipService.verifyFarmOwnership(farmId); // Opcional se a query já filtra

        return goatPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)
                .map(genealogyMapper::toResponseVO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cabra não encontrada ou não pertence à fazenda informada: " + goatId));
    }

    /**
     * @deprecated Genealogia agora é uma projeção automática. Este método existe apenas para manter compatibilidade com o frontend.
     * Retorna a projeção atual da genealogia sem persistir nada.
     */
    @Deprecated
    @Transactional(readOnly = true)
    @Override
    public GenealogyResponseVO createGenealogy(Long farmId, String goatId) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        
        // Comportamento simulado: apenas retorna a projeção existente
        return findGenealogy(farmId, goatId);
    }

    /**
     * @deprecated Genealogia agora é uma projeção automática. Este método existe apenas para manter compatibilidade com o frontend.
     * Simula a resposta esperada usando os dados fornecidos, mas NÃO persiste alterações.
     */
    @Deprecated
    @Transactional(readOnly = true)
    @Override
    public GenealogyResponseVO createGenealogyWithData(Long farmId, String goatId, GenealogyRequestDTO requestDTO) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        
        // Verifica consistência da URL
        if (!goatId.equals(requestDTO.getGoatRegistration())) {
            throw new IllegalArgumentException("O ID da cabra na URL não corresponde ao ID da cabra no corpo da requisição.");
        }

        Goat goat = goatPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + goatId));

        // Simula a aplicação dos dados do DTO em memória para retornar o JSON esperado pelo front
        // Nota: Isso NÃO salva no banco. Se o usuário quiser alterar pai/mãe, deve usar o endpoint de atualização de Goat.
        if (requestDTO.getFatherRegistration() != null && !requestDTO.getFatherRegistration().isBlank()) {
            goatPort.findByRegistrationNumber(requestDTO.getFatherRegistration())
                    .ifPresent(goat::setFather);
        }
        if (requestDTO.getMotherRegistration() != null && !requestDTO.getMotherRegistration().isBlank()) {
            goatPort.findByRegistrationNumber(requestDTO.getMotherRegistration())
                    .ifPresent(goat::setMother);
        }

        return genealogyMapper.toResponseVO(goat);
    }
}



