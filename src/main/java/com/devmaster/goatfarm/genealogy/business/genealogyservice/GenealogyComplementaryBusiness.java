package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyNodeSource;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeNode;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyComplementaryQueryUseCase;
import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyTreeProjectionUseCase;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryIntegrationVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryNodeVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryResponseVO;
import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;
import com.devmaster.goatfarm.goat.application.ports.in.GoatGenealogyReadUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Projects a technical local genealogy graph and enriches only missing local
 * ancestry with the external ABCC read boundary for public API consumers.
 */
@Service
public class GenealogyComplementaryBusiness implements GenealogyComplementaryQueryUseCase {

    private final GoatGenealogyReadUseCase goatGenealogyReadUseCase;
    private final GenealogyTreeProjectionUseCase genealogyTreeProjectionUseCase;

    public GenealogyComplementaryBusiness(
            GoatGenealogyReadUseCase goatGenealogyReadUseCase,
            GenealogyTreeProjectionUseCase genealogyTreeProjectionUseCase
    ) {
        this.goatGenealogyReadUseCase = goatGenealogyReadUseCase;
        this.genealogyTreeProjectionUseCase = genealogyTreeProjectionUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public GenealogyComplementaryResponseVO findComplementaryGenealogy(Long farmId, String registrationNumber) {
        GoatGenealogySnapshot goat = goatGenealogyReadUseCase
                .findGenealogyByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));

        GenealogyTreeSnapshot treeSnapshot = genealogyTreeProjectionUseCase.complementWithAbcc(goat);
        return toPublicResponse(treeSnapshot);
    }

    private GenealogyComplementaryResponseVO toPublicResponse(GenealogyTreeSnapshot snapshot) {
        return GenealogyComplementaryResponseVO.builder()
                .animalPrincipal(toPublicNode(snapshot.animalPrincipal()))
                .pai(toPublicNode(snapshot.pai()))
                .mae(toPublicNode(snapshot.mae()))
                .avoPaterno(toPublicNode(snapshot.avoPaterno()))
                .avoPaterna(toPublicNode(snapshot.avoPaterna()))
                .avoMaterno(toPublicNode(snapshot.avoMaterno()))
                .avoMaterna(toPublicNode(snapshot.avoMaterna()))
                .bisavoPaternoPai(toPublicNode(snapshot.bisavoPaternoPai()))
                .bisavoPaternaPai(toPublicNode(snapshot.bisavoPaternaPai()))
                .bisavoPaternoMae(toPublicNode(snapshot.bisavoPaternoMae()))
                .bisavoPaternaMae(toPublicNode(snapshot.bisavoPaternaMae()))
                .bisavoMaternoPai(toPublicNode(snapshot.bisavoMaternoPai()))
                .bisavoMaternaPai(toPublicNode(snapshot.bisavoMaternaPai()))
                .bisavoMaternoMae(toPublicNode(snapshot.bisavoMaternoMae()))
                .bisavoMaternaMae(toPublicNode(snapshot.bisavoMaternaMae()))
                .integration(snapshot.integration() == null ? null : GenealogyComplementaryIntegrationVO.builder()
                        .status(snapshot.integration().status())
                        .lookupKey(snapshot.integration().lookupKey())
                        .message(snapshot.integration().message())
                        .build())
                .build();
    }

    private GenealogyComplementaryNodeVO toPublicNode(GenealogyTreeNode node) {
        if (node == null) {
            return null;
        }
        return GenealogyComplementaryNodeVO.builder()
                .relationship(node.relationship())
                .name(node.name())
                .registrationNumber(node.registrationNumber())
                .source(node.source())
                .localGoatId(node.source() == GenealogyNodeSource.LOCAL ? node.registrationNumber() : null)
                .localTechnicalGoatId(node.localGoatId() == null ? null : node.localGoatId().value())
                .build();
    }
}
