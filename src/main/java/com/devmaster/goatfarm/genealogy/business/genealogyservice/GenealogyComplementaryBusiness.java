package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyComplementaryQueryUseCase;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryIntegrationVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryNodeVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryResponseVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyNodeSource;
import com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogyQueryPort;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GenealogyComplementaryBusiness implements GenealogyComplementaryQueryUseCase {

    private static final String LOOKUP_KEY = "registrationNumber";

    private final GoatGenealogyQueryPort goatGenealogyQueryPort;
    private final GenealogyAbccQueryPort genealogyAbccQueryPort;

    public GenealogyComplementaryBusiness(
            GoatGenealogyQueryPort goatGenealogyQueryPort,
            GenealogyAbccQueryPort genealogyAbccQueryPort
    ) {
        this.goatGenealogyQueryPort = goatGenealogyQueryPort;
        this.genealogyAbccQueryPort = genealogyAbccQueryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public GenealogyComplementaryResponseVO findComplementaryGenealogy(Long farmId, String goatId) {
        Goat goat = goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra nÃ£o encontrada para a fazenda informada."));

        if (isBlank(goat.getRegistrationNumber())) {
            return buildResponse(goat, null, integration("INSUFFICIENT_DATA",
                    "Registro do animal ausente ou invÃ¡lido para consulta complementar na ABCC."));
        }

        try {
            Optional<GenealogyAbccSnapshotVO> abccSnapshot = genealogyAbccQueryPort.findGenealogyByRegistrationNumber(
                    goat.getRegistrationNumber()
            );

            if (abccSnapshot.isEmpty()) {
                return buildResponse(goat, null, integration("NOT_FOUND",
                        "NÃ£o foi possÃ­vel localizar genealogia complementar na ABCC para este registro."));
            }

            return buildResponse(goat, abccSnapshot.get(), integration("FOUND",
                    "Genealogia complementar ABCC carregada com sucesso."));
        } catch (RuntimeException ex) {
            return buildResponse(goat, null, integration("UNAVAILABLE",
                    "NÃ£o foi possÃ­vel consultar a ABCC no momento. Exibindo apenas a genealogia local."));
        }
    }

    private GenealogyComplementaryResponseVO buildResponse(
            Goat root,
            GenealogyAbccSnapshotVO abcc,
            GenealogyComplementaryIntegrationVO integration
    ) {
        Goat paiLocal = root.getFather();
        Goat maeLocal = root.getMother();

        Goat avoPaternoLocal = paiLocal != null ? paiLocal.getFather() : null;
        Goat avoPaternaLocal = paiLocal != null ? paiLocal.getMother() : null;
        Goat avoMaternoLocal = maeLocal != null ? maeLocal.getFather() : null;
        Goat avoMaternaLocal = maeLocal != null ? maeLocal.getMother() : null;

        return GenealogyComplementaryResponseVO.builder()
                .animalPrincipal(buildNode("animalPrincipal", root, abcc != null ? abcc.getAnimalName() : null, abcc != null ? abcc.getAnimalRegistrationNumber() : null))
                .pai(buildNode("pai", paiLocal, abcc != null ? abcc.getFatherName() : null, abcc != null ? abcc.getFatherRegistrationNumber() : null))
                .mae(buildNode("mae", maeLocal, abcc != null ? abcc.getMotherName() : null, abcc != null ? abcc.getMotherRegistrationNumber() : null))
                .avoPaterno(buildNode("avoPaterno", avoPaternoLocal, abcc != null ? abcc.getPaternalGrandfatherName() : null, abcc != null ? abcc.getPaternalGrandfatherRegistrationNumber() : null))
                .avoPaterna(buildNode("avoPaterna", avoPaternaLocal, abcc != null ? abcc.getPaternalGrandmotherName() : null, abcc != null ? abcc.getPaternalGrandmotherRegistrationNumber() : null))
                .avoMaterno(buildNode("avoMaterno", avoMaternoLocal, abcc != null ? abcc.getMaternalGrandfatherName() : null, abcc != null ? abcc.getMaternalGrandfatherRegistrationNumber() : null))
                .avoMaterna(buildNode("avoMaterna", avoMaternaLocal, abcc != null ? abcc.getMaternalGrandmotherName() : null, abcc != null ? abcc.getMaternalGrandmotherRegistrationNumber() : null))
                .bisavoPaternoPai(buildNode("bisavoPaternoPai", avoPaternoLocal != null ? avoPaternoLocal.getFather() : null, abcc != null ? abcc.getBisavoPaternoPaiName() : null, abcc != null ? abcc.getBisavoPaternoPaiRegistrationNumber() : null))
                .bisavoPaternaPai(buildNode("bisavoPaternaPai", avoPaternoLocal != null ? avoPaternoLocal.getMother() : null, abcc != null ? abcc.getBisavoPaternaPaiName() : null, abcc != null ? abcc.getBisavoPaternaPaiRegistrationNumber() : null))
                .bisavoPaternoMae(buildNode("bisavoPaternoMae", avoPaternaLocal != null ? avoPaternaLocal.getFather() : null, abcc != null ? abcc.getBisavoPaternoMaeName() : null, abcc != null ? abcc.getBisavoPaternoMaeRegistrationNumber() : null))
                .bisavoPaternaMae(buildNode("bisavoPaternaMae", avoPaternaLocal != null ? avoPaternaLocal.getMother() : null, abcc != null ? abcc.getBisavoPaternaMaeName() : null, abcc != null ? abcc.getBisavoPaternaMaeRegistrationNumber() : null))
                .bisavoMaternoPai(buildNode("bisavoMaternoPai", avoMaternoLocal != null ? avoMaternoLocal.getFather() : null, abcc != null ? abcc.getBisavoMaternoPaiName() : null, abcc != null ? abcc.getBisavoMaternoPaiRegistrationNumber() : null))
                .bisavoMaternaPai(buildNode("bisavoMaternaPai", avoMaternoLocal != null ? avoMaternoLocal.getMother() : null, abcc != null ? abcc.getBisavoMaternaPaiName() : null, abcc != null ? abcc.getBisavoMaternaPaiRegistrationNumber() : null))
                .bisavoMaternoMae(buildNode("bisavoMaternoMae", avoMaternaLocal != null ? avoMaternaLocal.getFather() : null, abcc != null ? abcc.getBisavoMaternoMaeName() : null, abcc != null ? abcc.getBisavoMaternoMaeRegistrationNumber() : null))
                .bisavoMaternaMae(buildNode("bisavoMaternaMae", avoMaternaLocal != null ? avoMaternaLocal.getMother() : null, abcc != null ? abcc.getBisavoMaternaMaeName() : null, abcc != null ? abcc.getBisavoMaternaMaeRegistrationNumber() : null))
                .integration(integration)
                .build();
    }

    private GenealogyComplementaryNodeVO buildNode(
            String relationship,
            Goat localGoat,
            String abccName,
            String abccRegistrationNumber
    ) {
        if (localGoat != null) {
            return GenealogyComplementaryNodeVO.builder()
                    .relationship(relationship)
                    .name(localGoat.getName())
                    .registrationNumber(localGoat.getRegistrationNumber())
                    .source(GenealogyNodeSource.LOCAL)
                    .localGoatId(localGoat.getRegistrationNumber())
                    .build();
        }

        if (!isBlank(abccName) || !isBlank(abccRegistrationNumber)) {
            return GenealogyComplementaryNodeVO.builder()
                    .relationship(relationship)
                    .name(trimOrNull(abccName))
                    .registrationNumber(trimOrNull(abccRegistrationNumber))
                    .source(GenealogyNodeSource.ABCC)
                    .localGoatId(null)
                    .build();
        }

        return GenealogyComplementaryNodeVO.builder()
                .relationship(relationship)
                .name(null)
                .registrationNumber(null)
                .source(GenealogyNodeSource.AUSENTE)
                .localGoatId(null)
                .build();
    }

    private GenealogyComplementaryIntegrationVO integration(String status, String message) {
        return GenealogyComplementaryIntegrationVO.builder()
                .status(status)
                .lookupKey(LOOKUP_KEY)
                .message(message)
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

