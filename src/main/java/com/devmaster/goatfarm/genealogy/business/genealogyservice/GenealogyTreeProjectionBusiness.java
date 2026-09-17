package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.genealogy.application.model.GenealogyIntegrationSnapshot;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyNodeSource;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeNode;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyTreeProjectionUseCase;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for building and enriching local and ABCC-complemented
 * genealogy trees according to domain precedence rules.
 */
@Service
public class GenealogyTreeProjectionBusiness implements GenealogyTreeProjectionUseCase {

    private static final String LOOKUP_KEY = "registrationNumber";

    private enum AbccLookupStatus {
        FOUND,
        NOT_FOUND,
        UNAVAILABLE
    }

    private record AbccLookupResult(AbccLookupStatus status, GenealogyAbccSnapshotVO snapshot) {}

    private final GenealogyAbccQueryPort genealogyAbccQueryPort;

    public GenealogyTreeProjectionBusiness(GenealogyAbccQueryPort genealogyAbccQueryPort) {
        this.genealogyAbccQueryPort = genealogyAbccQueryPort;
    }

    @Override
    public GenealogyTreeSnapshot projectLocal(GoatGenealogySnapshot root) {
        return buildSnapshot(root, false, new HashMap<>());
    }

    @Override
    public GenealogyTreeSnapshot complementWithAbcc(GoatGenealogySnapshot root) {
        Map<String, AbccLookupResult> cache = new HashMap<>();

        GenealogyTreeSnapshot snapshot = buildSnapshot(root, true, cache);

        String status = deriveIntegrationStatus(cache);
        String message = buildIntegrationMessage(status);
        GenealogyIntegrationSnapshot integration = integration(status, message);

        return attachIntegration(snapshot, integration);
    }

    private String deriveIntegrationStatus(Map<String, AbccLookupResult> cache) {
        if (cache.isEmpty()) {
            return "INSUFFICIENT_DATA";
        }

        boolean hasFound = false;
        boolean hasUnavailable = false;
        boolean hasNotFound = false;

        for (AbccLookupResult res : cache.values()) {
            if (res.status() == AbccLookupStatus.FOUND) {
                hasFound = true;
            } else if (res.status() == AbccLookupStatus.UNAVAILABLE) {
                hasUnavailable = true;
            } else if (res.status() == AbccLookupStatus.NOT_FOUND) {
                hasNotFound = true;
            }
        }

        if (hasFound) {
            return "FOUND";
        }
        if (hasUnavailable) {
            return "UNAVAILABLE";
        }
        if (hasNotFound) {
            return "NOT_FOUND";
        }
        return "INSUFFICIENT_DATA";
    }

    private String buildIntegrationMessage(String status) {
        return switch (status) {
            case "FOUND" -> "Genealogia complementar ABCC carregada com sucesso.";
            case "NOT_FOUND" -> "Não foi possível localizar genealogia complementar na ABCC para este registro.";
            case "UNAVAILABLE" -> "Não foi possível consultar a ABCC no momento. Exibindo apenas a genealogia local.";
            default -> "Registro do animal ausente ou inválido para consulta complementar na ABCC.";
        };
    }

    private GenealogyTreeSnapshot attachIntegration(
            GenealogyTreeSnapshot snapshot,
            GenealogyIntegrationSnapshot integration
    ) {
        return new GenealogyTreeSnapshot(
                snapshot.animalPrincipal(),
                snapshot.pai(),
                snapshot.mae(),
                snapshot.avoPaterno(),
                snapshot.avoPaterna(),
                snapshot.avoMaterno(),
                snapshot.avoMaterna(),
                snapshot.bisavoPaternoPai(),
                snapshot.bisavoPaternaPai(),
                snapshot.bisavoPaternoMae(),
                snapshot.bisavoPaternaMae(),
                snapshot.bisavoMaternoPai(),
                snapshot.bisavoMaternaPai(),
                snapshot.bisavoMaternoMae(),
                snapshot.bisavoMaternaMae(),
                integration
        );
    }

    private GenealogyTreeSnapshot buildSnapshot(
            GoatGenealogySnapshot root,
            boolean enableAbcc,
            Map<String, AbccLookupResult> cache
    ) {
        // Generation 0: Root
        GoatGenealogySnapshot localRoot = root;
        String rootRg = root == null ? null : root.registrationNumber();

        AbccLookupResult rootLookupRes = enableAbcc ? findAbccResult(rootRg, cache) : new AbccLookupResult(AbccLookupStatus.NOT_FOUND, null);
        GenealogyAbccSnapshotVO abccRoot = rootLookupRes.status() == AbccLookupStatus.FOUND ? rootLookupRes.snapshot() : null;

        GenealogyTreeNode animalPrincipal = buildNode(
                "animalPrincipal",
                localRoot,
                null,
                abccRoot == null ? null : abccRoot.getAnimalName(),
                abccRoot == null ? null : abccRoot.getAnimalRegistrationNumber()
        );

        // Generation 1: Parents
        GoatGenealogySnapshot localFather = localGoat(root == null ? null : root.father());
        String declaredFatherRg = externalRegistrationNumber(root == null ? null : root.father());

        GoatGenealogySnapshot localMother = localGoat(root == null ? null : root.mother());
        String declaredMotherRg = externalRegistrationNumber(root == null ? null : root.mother());

        boolean rootPaternalBranchEligible = enableAbcc && abccRoot != null && (
                localFather != null ? isMatchingRegistration(localFather.registrationNumber(), abccRoot.getFatherRegistrationNumber()) :
                !isBlank(declaredFatherRg) ? isMatchingRegistration(declaredFatherRg, abccRoot.getFatherRegistrationNumber()) :
                true
        );

        boolean rootMaternalBranchEligible = enableAbcc && abccRoot != null && (
                localMother != null ? isMatchingRegistration(localMother.registrationNumber(), abccRoot.getMotherRegistrationNumber()) :
                !isBlank(declaredMotherRg) ? isMatchingRegistration(declaredMotherRg, abccRoot.getMotherRegistrationNumber()) :
                true
        );

        ResolvedNodeEnrichment fatherEnrichment = resolveNodeEnrichment(
                localFather,
                declaredFatherRg,
                null,
                true,
                true,
                abccRoot == null ? null : abccRoot.getFatherName(),
                abccRoot == null ? null : abccRoot.getFatherRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment motherEnrichment = resolveNodeEnrichment(
                localMother,
                declaredMotherRg,
                null,
                false,
                true,
                abccRoot == null ? null : abccRoot.getMotherName(),
                abccRoot == null ? null : abccRoot.getMotherRegistrationNumber(),
                enableAbcc,
                cache
        );

        GenealogyTreeNode fatherNode = buildNode("pai", localFather, declaredFatherRg, fatherEnrichment.name(), fatherEnrichment.rg());
        GenealogyTreeNode motherNode = buildNode("mae", localMother, declaredMotherRg, motherEnrichment.name(), motherEnrichment.rg());

        // Generation 2: Grandparents
        GoatGenealogySnapshot localAvoPaterno = localGoat(localFather == null ? null : localFather.father());
        String declaredAvoPaternoRg = externalRegistrationNumber(localFather == null ? null : localFather.father());

        GoatGenealogySnapshot localAvoPaterna = localGoat(localFather == null ? null : localFather.mother());
        String declaredAvoPaternaRg = externalRegistrationNumber(localFather == null ? null : localFather.mother());

        GoatGenealogySnapshot localAvoMaterno = localGoat(localMother == null ? null : localMother.father());
        String declaredAvoMaternoRg = externalRegistrationNumber(localMother == null ? null : localMother.father());

        GoatGenealogySnapshot localAvoMaterna = localGoat(localMother == null ? null : localMother.mother());
        String declaredAvoMaternaRg = externalRegistrationNumber(localMother == null ? null : localMother.mother());

        ResolvedNodeEnrichment avoPaternoEnrichment = resolveNodeEnrichment(
                localAvoPaterno,
                declaredAvoPaternoRg,
                fatherEnrichment.dedicatedProvider(),
                true,
                rootPaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getPaternalGrandfatherName(),
                abccRoot == null ? null : abccRoot.getPaternalGrandfatherRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment avoPaternaEnrichment = resolveNodeEnrichment(
                localAvoPaterna,
                declaredAvoPaternaRg,
                fatherEnrichment.dedicatedProvider(),
                false,
                rootPaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getPaternalGrandmotherName(),
                abccRoot == null ? null : abccRoot.getPaternalGrandmotherRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment avoMaternoEnrichment = resolveNodeEnrichment(
                localAvoMaterno,
                declaredAvoMaternoRg,
                motherEnrichment.dedicatedProvider(),
                true,
                rootMaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getMaternalGrandfatherName(),
                abccRoot == null ? null : abccRoot.getMaternalGrandfatherRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment avoMaternaEnrichment = resolveNodeEnrichment(
                localAvoMaterna,
                declaredAvoMaternaRg,
                motherEnrichment.dedicatedProvider(),
                false,
                rootMaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getMaternalGrandmotherName(),
                abccRoot == null ? null : abccRoot.getMaternalGrandmotherRegistrationNumber(),
                enableAbcc,
                cache
        );

        GenealogyTreeNode avoPaternoNode = buildNode("avoPaterno", localAvoPaterno, declaredAvoPaternoRg, avoPaternoEnrichment.name(), avoPaternoEnrichment.rg());
        GenealogyTreeNode avoPaternaNode = buildNode("avoPaterna", localAvoPaterna, declaredAvoPaternaRg, avoPaternaEnrichment.name(), avoPaternaEnrichment.rg());
        GenealogyTreeNode avoMaternoNode = buildNode("avoMaterno", localAvoMaterno, declaredAvoMaternoRg, avoMaternoEnrichment.name(), avoMaternoEnrichment.rg());
        GenealogyTreeNode avoMaternaNode = buildNode("avoMaterna", localAvoMaterna, declaredAvoMaternaRg, avoMaternaEnrichment.name(), avoMaternaEnrichment.rg());

        // Generation 3: Great-Grandparents
        GoatGenealogySnapshot localBisavoPaternoPai = localGoat(localAvoPaterno == null ? null : localAvoPaterno.father());
        String declaredBisavoPaternoPaiRg = externalRegistrationNumber(localAvoPaterno == null ? null : localAvoPaterno.father());

        GoatGenealogySnapshot localBisavoPaternaPai = localGoat(localAvoPaterno == null ? null : localAvoPaterno.mother());
        String declaredBisavoPaternaPaiRg = externalRegistrationNumber(localAvoPaterno == null ? null : localAvoPaterno.mother());

        GoatGenealogySnapshot localBisavoPaternoMae = localGoat(localAvoPaterna == null ? null : localAvoPaterna.father());
        String declaredBisavoPaternoMaeRg = externalRegistrationNumber(localAvoPaterna == null ? null : localAvoPaterna.father());

        GoatGenealogySnapshot localBisavoPaternaMae = localGoat(localAvoPaterna == null ? null : localAvoPaterna.mother());
        String declaredBisavoPaternaMaeRg = externalRegistrationNumber(localAvoPaterna == null ? null : localAvoPaterna.mother());

        GoatGenealogySnapshot localBisavoMaternoPai = localGoat(localAvoMaterno == null ? null : localAvoMaterno.father());
        String declaredBisavoMaternoPaiRg = externalRegistrationNumber(localAvoMaterno == null ? null : localAvoMaterno.father());

        GoatGenealogySnapshot localBisavoMaternaPai = localGoat(localAvoMaterno == null ? null : localAvoMaterno.mother());
        String declaredBisavoMaternaPaiRg = externalRegistrationNumber(localAvoMaterno == null ? null : localAvoMaterno.mother());

        GoatGenealogySnapshot localBisavoMaternoMae = localGoat(localAvoMaterna == null ? null : localAvoMaterna.father());
        String declaredBisavoMaternoMaeRg = externalRegistrationNumber(localAvoMaterna == null ? null : localAvoMaterna.father());

        GoatGenealogySnapshot localBisavoMaternaMae = localGoat(localAvoMaterna == null ? null : localAvoMaterna.mother());
        String declaredBisavoMaternaMaeRg = externalRegistrationNumber(localAvoMaterna == null ? null : localAvoMaterna.mother());

        ResolvedNodeEnrichment bisavoPaternoPaiEnrichment = resolveNodeEnrichment(
                localBisavoPaternoPai,
                declaredBisavoPaternoPaiRg,
                avoPaternoEnrichment.dedicatedProvider(),
                true,
                rootPaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getBisavoPaternoPaiName(),
                abccRoot == null ? null : abccRoot.getBisavoPaternoPaiRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment bisavoPaternaPaiEnrichment = resolveNodeEnrichment(
                localBisavoPaternaPai,
                declaredBisavoPaternaPaiRg,
                avoPaternoEnrichment.dedicatedProvider(),
                false,
                rootPaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getBisavoPaternaPaiName(),
                abccRoot == null ? null : abccRoot.getBisavoPaternaPaiRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment bisavoPaternoMaeEnrichment = resolveNodeEnrichment(
                localBisavoPaternoMae,
                declaredBisavoPaternoMaeRg,
                avoPaternaEnrichment.dedicatedProvider(),
                true,
                rootPaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getBisavoPaternoMaeName(),
                abccRoot == null ? null : abccRoot.getBisavoPaternoMaeRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment bisavoPaternaMaeEnrichment = resolveNodeEnrichment(
                localBisavoPaternaMae,
                declaredBisavoPaternaMaeRg,
                avoPaternaEnrichment.dedicatedProvider(),
                false,
                rootPaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getBisavoPaternaMaeName(),
                abccRoot == null ? null : abccRoot.getBisavoPaternaMaeRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment bisavoMaternoPaiEnrichment = resolveNodeEnrichment(
                localBisavoMaternoPai,
                declaredBisavoMaternoPaiRg,
                avoMaternoEnrichment.dedicatedProvider(),
                true,
                rootMaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getBisavoMaternoPaiName(),
                abccRoot == null ? null : abccRoot.getBisavoMaternoPaiRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment bisavoMaternaPaiEnrichment = resolveNodeEnrichment(
                localBisavoMaternaPai,
                declaredBisavoMaternaPaiRg,
                avoMaternoEnrichment.dedicatedProvider(),
                false,
                rootMaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getBisavoMaternaPaiName(),
                abccRoot == null ? null : abccRoot.getBisavoMaternaPaiRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment bisavoMaternoMaeEnrichment = resolveNodeEnrichment(
                localBisavoMaternoMae,
                declaredBisavoMaternoMaeRg,
                avoMaternaEnrichment.dedicatedProvider(),
                true,
                rootMaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getBisavoMaternoMaeName(),
                abccRoot == null ? null : abccRoot.getBisavoMaternoMaeRegistrationNumber(),
                enableAbcc,
                cache
        );

        ResolvedNodeEnrichment bisavoMaternaMaeEnrichment = resolveNodeEnrichment(
                localBisavoMaternaMae,
                declaredBisavoMaternaMaeRg,
                avoMaternaEnrichment.dedicatedProvider(),
                false,
                rootMaternalBranchEligible,
                abccRoot == null ? null : abccRoot.getBisavoMaternaMaeName(),
                abccRoot == null ? null : abccRoot.getBisavoMaternaMaeRegistrationNumber(),
                enableAbcc,
                cache
        );

        return new GenealogyTreeSnapshot(
                animalPrincipal,
                fatherNode,
                motherNode,
                avoPaternoNode,
                avoPaternaNode,
                avoMaternoNode,
                avoMaternaNode,
                buildNode("bisavoPaternoPai", localBisavoPaternoPai, declaredBisavoPaternoPaiRg, bisavoPaternoPaiEnrichment.name(), bisavoPaternoPaiEnrichment.rg()),
                buildNode("bisavoPaternaPai", localBisavoPaternaPai, declaredBisavoPaternaPaiRg, bisavoPaternaPaiEnrichment.name(), bisavoPaternaPaiEnrichment.rg()),
                buildNode("bisavoPaternoMae", localBisavoPaternoMae, declaredBisavoPaternoMaeRg, bisavoPaternoMaeEnrichment.name(), bisavoPaternoMaeEnrichment.rg()),
                buildNode("bisavoPaternaMae", localBisavoPaternaMae, declaredBisavoPaternaMaeRg, bisavoPaternaMaeEnrichment.name(), bisavoPaternaMaeEnrichment.rg()),
                buildNode("bisavoMaternoPai", localBisavoMaternoPai, declaredBisavoMaternoPaiRg, bisavoMaternoPaiEnrichment.name(), bisavoMaternoPaiEnrichment.rg()),
                buildNode("bisavoMaternaPai", localBisavoMaternaPai, declaredBisavoMaternaPaiRg, bisavoMaternaPaiEnrichment.name(), bisavoMaternaPaiEnrichment.rg()),
                buildNode("bisavoMaternoMae", localBisavoMaternoMae, declaredBisavoMaternoMaeRg, bisavoMaternoMaeEnrichment.name(), bisavoMaternoMaeEnrichment.rg()),
                buildNode("bisavoMaternaMae", localBisavoMaternaMae, declaredBisavoMaternaMaeRg, bisavoMaternaMaeEnrichment.name(), bisavoMaternaMaeEnrichment.rg()),
                null
        );
    }

    private record ResolvedNodeEnrichment(
            String name,
            String rg,
            GenealogyAbccSnapshotVO dedicatedProvider
    ) {}

    private ResolvedNodeEnrichment resolveNodeEnrichment(
            GoatGenealogySnapshot localGoat,
            String declaredRg,
            GenealogyAbccSnapshotVO intermediateProvider,
            boolean isFatherSideOfIntermediate,
            boolean isRootBranchEligible,
            String rootFallbackName,
            String rootFallbackRg,
            boolean enableAbcc,
            Map<String, AbccLookupResult> cache
    ) {
        if (!enableAbcc) {
            return new ResolvedNodeEnrichment(null, declaredRg, null);
        }

        if (localGoat != null) {
            String canonicalRg = localGoat.registrationNumber();
            AbccLookupResult dedicatedRes = findAbccResult(canonicalRg, cache);
            GenealogyAbccSnapshotVO dedicatedVO = dedicatedRes.status() == AbccLookupStatus.FOUND ? dedicatedRes.snapshot() : null;
            return new ResolvedNodeEnrichment(localGoat.name(), canonicalRg, dedicatedVO);
        }

        if (!isBlank(declaredRg)) {
            String trimmedDeclaredRg = declaredRg.trim();
            AbccLookupResult dedicatedRes = findAbccResult(trimmedDeclaredRg, cache);
            if (dedicatedRes.status() == AbccLookupStatus.FOUND && dedicatedRes.snapshot() != null) {
                GenealogyAbccSnapshotVO dedicatedVO = dedicatedRes.snapshot();
                return new ResolvedNodeEnrichment(dedicatedVO.getAnimalName(), trimmedDeclaredRg, dedicatedVO);
            }

            if (intermediateProvider != null) {
                String interRg = isFatherSideOfIntermediate ? intermediateProvider.getFatherRegistrationNumber() : intermediateProvider.getMotherRegistrationNumber();
                String interName = isFatherSideOfIntermediate ? intermediateProvider.getFatherName() : intermediateProvider.getMotherName();
                if (isMatchingRegistration(trimmedDeclaredRg, interRg)) {
                    return new ResolvedNodeEnrichment(interName, trimmedDeclaredRg, null);
                }
            }

            if (isRootBranchEligible && isMatchingRegistration(trimmedDeclaredRg, rootFallbackRg)) {
                return new ResolvedNodeEnrichment(rootFallbackName, trimmedDeclaredRg, null);
            }

            return new ResolvedNodeEnrichment(null, trimmedDeclaredRg, null);
        }

        if (intermediateProvider != null) {
            String interName = isFatherSideOfIntermediate ? intermediateProvider.getFatherName() : intermediateProvider.getMotherName();
            String interRg = isFatherSideOfIntermediate ? intermediateProvider.getFatherRegistrationNumber() : intermediateProvider.getMotherRegistrationNumber();

            if (!isBlank(interName) || !isBlank(interRg)) {
                AbccLookupResult dedicatedRes = findAbccResult(interRg, cache);
                GenealogyAbccSnapshotVO dedicatedVO = dedicatedRes.status() == AbccLookupStatus.FOUND ? dedicatedRes.snapshot() : null;
                return new ResolvedNodeEnrichment(interName, interRg, dedicatedVO);
            }
        }

        if (isRootBranchEligible && (!isBlank(rootFallbackName) || !isBlank(rootFallbackRg))) {
            AbccLookupResult dedicatedRes = findAbccResult(rootFallbackRg, cache);
            GenealogyAbccSnapshotVO dedicatedVO = dedicatedRes.status() == AbccLookupStatus.FOUND ? dedicatedRes.snapshot() : null;
            return new ResolvedNodeEnrichment(rootFallbackName, rootFallbackRg, dedicatedVO);
        }

        return new ResolvedNodeEnrichment(null, null, null);
    }

    private GenealogyTreeNode buildNode(
            String relationship,
            GoatGenealogySnapshot localGoat,
            String declaredRegistration,
            String abccName,
            String abccRegistrationNumber
    ) {
        if (localGoat != null) {
            return new GenealogyTreeNode(
                    relationship,
                    localGoat.name(),
                    localGoat.registrationNumber(),
                    GenealogyNodeSource.LOCAL,
                    localGoat.id()
            );
        }

        if (!isBlank(declaredRegistration)) {
            return new GenealogyTreeNode(
                    relationship,
                    trimOrNull(abccName),
                    trimOrNull(declaredRegistration),
                    GenealogyNodeSource.DECLARADO,
                    null
            );
        }

        if (!isBlank(abccName) || !isBlank(abccRegistrationNumber)) {
            return new GenealogyTreeNode(
                    relationship,
                    trimOrNull(abccName),
                    trimOrNull(abccRegistrationNumber),
                    GenealogyNodeSource.ABCC,
                    null
            );
        }

        return new GenealogyTreeNode(
                relationship,
                null,
                null,
                GenealogyNodeSource.AUSENTE,
                null
        );
    }

    private GoatGenealogySnapshot localGoat(GoatGenealogySnapshot.ParentReference parent) {
        return parent == null ? null : parent.localGoat();
    }

    private String externalRegistrationNumber(GoatGenealogySnapshot.ParentReference parent) {
        return parent == null ? null : parent.externalRegistrationNumber();
    }

    private GenealogyIntegrationSnapshot integration(String status, String message) {
        return new GenealogyIntegrationSnapshot(status, LOOKUP_KEY, message);
    }

    private AbccLookupResult findAbccResult(String rg, Map<String, AbccLookupResult> cache) {
        if (isBlank(rg)) {
            return new AbccLookupResult(AbccLookupStatus.NOT_FOUND, null);
        }
        String key = rg.trim();
        return cache.computeIfAbsent(key, k -> {
            try {
                Optional<GenealogyAbccSnapshotVO> opt = genealogyAbccQueryPort.findGenealogyByRegistrationNumber(k);
                return opt.map(vo -> new AbccLookupResult(AbccLookupStatus.FOUND, vo))
                        .orElseGet(() -> new AbccLookupResult(AbccLookupStatus.NOT_FOUND, null));
            } catch (RuntimeException ex) {
                return new AbccLookupResult(AbccLookupStatus.UNAVAILABLE, null);
            }
        });
    }

    private boolean isMatchingRegistration(String rg1, String rg2) {
        if (isBlank(rg1) || isBlank(rg2)) {
            return false;
        }
        return rg1.trim().equalsIgnoreCase(rg2.trim());
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
