package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyComplementaryQueryUseCase;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryIntegrationVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryNodeVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyComplementaryResponseVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyNodeSource;
import com.devmaster.goatfarm.goat.application.ports.in.GoatGenealogyReadUseCase;
import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Projects a technical local genealogy graph and enriches only missing local
 * ancestry with the external ABCC read boundary.
 */
@Service
public class GenealogyComplementaryBusiness implements GenealogyComplementaryQueryUseCase {

    private static final String LOOKUP_KEY = "registrationNumber";

    private final GoatGenealogyReadUseCase goatGenealogyReadUseCase;
    private final GenealogyAbccQueryPort genealogyAbccQueryPort;

    public GenealogyComplementaryBusiness(
            GoatGenealogyReadUseCase goatGenealogyReadUseCase,
            GenealogyAbccQueryPort genealogyAbccQueryPort
    ) {
        this.goatGenealogyReadUseCase = goatGenealogyReadUseCase;
        this.genealogyAbccQueryPort = genealogyAbccQueryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public GenealogyComplementaryResponseVO findComplementaryGenealogy(Long farmId, String registrationNumber) {
        GoatGenealogySnapshot goat = goatGenealogyReadUseCase
                .findGenealogyByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));

        if (isBlank(goat.registrationNumber())) {
            return buildResponse(goat, null, null, null, integration("INSUFFICIENT_DATA",
                    "Registro do animal ausente ou inválido para consulta complementar na ABCC."));
        }

        try {
            Optional<GenealogyAbccSnapshotVO> abccSnapshot = genealogyAbccQueryPort
                    .findGenealogyByRegistrationNumber(goat.registrationNumber());

            if (abccSnapshot.isEmpty()) {
                return buildResponse(
                        goat,
                        null,
                        findExternalParentSnapshot(externalFatherOf(goat)),
                        findExternalParentSnapshot(externalMotherOf(goat)),
                        integration("NOT_FOUND", "Não foi possível localizar genealogia complementar na ABCC para este registro.")
                );
            }

            return buildResponse(goat, abccSnapshot.get(), null, null, integration("FOUND",
                    "Genealogia complementar ABCC carregada com sucesso."));
        } catch (RuntimeException ex) {
            return buildResponse(goat, null, null, null, integration("UNAVAILABLE",
                    "Não foi possível consultar a ABCC no momento. Exibindo apenas a genealogia local."));
        }
    }

    private GenealogyComplementaryResponseVO buildResponse(
            GoatGenealogySnapshot root,
            GenealogyAbccSnapshotVO abcc,
            GenealogyAbccSnapshotVO externalFather,
            GenealogyAbccSnapshotVO externalMother,
            GenealogyComplementaryIntegrationVO integration
    ) {
        GoatGenealogySnapshot father = fatherOf(root);
        GoatGenealogySnapshot mother = motherOf(root);
        GoatGenealogySnapshot paternalGrandfather = fatherOf(father);
        GoatGenealogySnapshot paternalGrandmother = motherOf(father);
        GoatGenealogySnapshot maternalGrandfather = fatherOf(mother);
        GoatGenealogySnapshot maternalGrandmother = motherOf(mother);

        return GenealogyComplementaryResponseVO.builder()
                .animalPrincipal(buildNode("animalPrincipal", root,
                        abcc == null ? null : abcc.getAnimalName(),
                        abcc == null ? null : abcc.getAnimalRegistrationNumber()))
                .pai(buildNode("pai", father,
                        abcc != null ? abcc.getFatherName() : externalFather == null ? null : externalFather.getAnimalName(),
                        abcc != null ? abcc.getFatherRegistrationNumber() : externalFather == null ? externalFatherOf(root) : externalFather.getAnimalRegistrationNumber(),
                        abcc == null && externalFather == null && !isBlank(externalFatherOf(root))))
                .mae(buildNode("mae", mother,
                        abcc != null ? abcc.getMotherName() : externalMother == null ? null : externalMother.getAnimalName(),
                        abcc != null ? abcc.getMotherRegistrationNumber() : externalMother == null ? externalMotherOf(root) : externalMother.getAnimalRegistrationNumber(),
                        abcc == null && externalMother == null && !isBlank(externalMotherOf(root))))
                .avoPaterno(buildNode("avoPaterno", paternalGrandfather,
                        abcc == null ? null : abcc.getPaternalGrandfatherName(),
                        abcc == null ? null : abcc.getPaternalGrandfatherRegistrationNumber()))
                .avoPaterna(buildNode("avoPaterna", paternalGrandmother,
                        abcc == null ? null : abcc.getPaternalGrandmotherName(),
                        abcc == null ? null : abcc.getPaternalGrandmotherRegistrationNumber()))
                .avoMaterno(buildNode("avoMaterno", maternalGrandfather,
                        abcc == null ? null : abcc.getMaternalGrandfatherName(),
                        abcc == null ? null : abcc.getMaternalGrandfatherRegistrationNumber()))
                .avoMaterna(buildNode("avoMaterna", maternalGrandmother,
                        abcc == null ? null : abcc.getMaternalGrandmotherName(),
                        abcc == null ? null : abcc.getMaternalGrandmotherRegistrationNumber()))
                .bisavoPaternoPai(buildNode("bisavoPaternoPai", fatherOf(paternalGrandfather),
                        abcc == null ? null : abcc.getBisavoPaternoPaiName(),
                        abcc == null ? null : abcc.getBisavoPaternoPaiRegistrationNumber()))
                .bisavoPaternaPai(buildNode("bisavoPaternaPai", motherOf(paternalGrandfather),
                        abcc == null ? null : abcc.getBisavoPaternaPaiName(),
                        abcc == null ? null : abcc.getBisavoPaternaPaiRegistrationNumber()))
                .bisavoPaternoMae(buildNode("bisavoPaternoMae", fatherOf(paternalGrandmother),
                        abcc == null ? null : abcc.getBisavoPaternoMaeName(),
                        abcc == null ? null : abcc.getBisavoPaternoMaeRegistrationNumber()))
                .bisavoPaternaMae(buildNode("bisavoPaternaMae", motherOf(paternalGrandmother),
                        abcc == null ? null : abcc.getBisavoPaternaMaeName(),
                        abcc == null ? null : abcc.getBisavoPaternaMaeRegistrationNumber()))
                .bisavoMaternoPai(buildNode("bisavoMaternoPai", fatherOf(maternalGrandfather),
                        abcc == null ? null : abcc.getBisavoMaternoPaiName(),
                        abcc == null ? null : abcc.getBisavoMaternoPaiRegistrationNumber()))
                .bisavoMaternaPai(buildNode("bisavoMaternaPai", motherOf(maternalGrandfather),
                        abcc == null ? null : abcc.getBisavoMaternaPaiName(),
                        abcc == null ? null : abcc.getBisavoMaternaPaiRegistrationNumber()))
                .bisavoMaternoMae(buildNode("bisavoMaternoMae", fatherOf(maternalGrandmother),
                        abcc == null ? null : abcc.getBisavoMaternoMaeName(),
                        abcc == null ? null : abcc.getBisavoMaternoMaeRegistrationNumber()))
                .bisavoMaternaMae(buildNode("bisavoMaternaMae", motherOf(maternalGrandmother),
                        abcc == null ? null : abcc.getBisavoMaternaMaeName(),
                        abcc == null ? null : abcc.getBisavoMaternaMaeRegistrationNumber()))
                .integration(integration)
                .build();
    }

    private GenealogyComplementaryNodeVO buildNode(
            String relationship,
            GoatGenealogySnapshot localGoat,
            String abccName,
            String abccRegistrationNumber
    ) {
        return buildNode(relationship, localGoat, abccName, abccRegistrationNumber, false);
    }

    private GenealogyComplementaryNodeVO buildNode(
            String relationship,
            GoatGenealogySnapshot localGoat,
            String abccName,
            String abccRegistrationNumber,
            boolean declared
    ) {
        if (localGoat != null) {
            Long technicalId = localGoat.id() == null ? null : localGoat.id().value();
            return GenealogyComplementaryNodeVO.builder()
                    .relationship(relationship)
                    .name(localGoat.name())
                    .registrationNumber(localGoat.registrationNumber())
                    .source(GenealogyNodeSource.LOCAL)
                    // Legacy response field: retains its historical RG value.
                    .localGoatId(localGoat.registrationNumber())
                    .localTechnicalGoatId(technicalId)
                    .build();
        }

        if (!isBlank(abccName) || !isBlank(abccRegistrationNumber)) {
            return GenealogyComplementaryNodeVO.builder()
                    .relationship(relationship)
                    .name(trimOrNull(abccName))
                    .registrationNumber(trimOrNull(abccRegistrationNumber))
                    .source(declared ? GenealogyNodeSource.DECLARADO : GenealogyNodeSource.ABCC)
                    .localGoatId(null)
                    .localTechnicalGoatId(null)
                    .build();
        }

        return GenealogyComplementaryNodeVO.builder()
                .relationship(relationship)
                .name(null)
                .registrationNumber(null)
                .source(GenealogyNodeSource.AUSENTE)
                .localGoatId(null)
                .localTechnicalGoatId(null)
                .build();
    }

    private GoatGenealogySnapshot fatherOf(GoatGenealogySnapshot goat) {
        return localGoat(goat == null ? null : goat.father());
    }

    private GoatGenealogySnapshot motherOf(GoatGenealogySnapshot goat) {
        return localGoat(goat == null ? null : goat.mother());
    }

    private GoatGenealogySnapshot localGoat(GoatGenealogySnapshot.ParentReference parent) {
        return parent == null ? null : parent.localGoat();
    }

    private String externalFatherOf(GoatGenealogySnapshot goat) {
        return goat == null || goat.father() == null ? null : goat.father().externalRegistrationNumber();
    }

    private String externalMotherOf(GoatGenealogySnapshot goat) {
        return goat == null || goat.mother() == null ? null : goat.mother().externalRegistrationNumber();
    }

    private GenealogyComplementaryIntegrationVO integration(String status, String message) {
        return GenealogyComplementaryIntegrationVO.builder()
                .status(status)
                .lookupKey(LOOKUP_KEY)
                .message(message)
                .build();
    }

    private GenealogyAbccSnapshotVO findExternalParentSnapshot(String registrationNumber) {
        if (isBlank(registrationNumber)) {
            return null;
        }
        try {
            return genealogyAbccQueryPort.findGenealogyByRegistrationNumber(registrationNumber).orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
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
