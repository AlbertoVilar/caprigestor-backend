package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccImportUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.model.GoatCreationOrigin;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatCreatorProvenanceVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.abcc.AbccImportEligibilityPolicy;
import com.devmaster.goatfarm.goat.business.abcc.AbccAnimalTranslator;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class GoatAbccImportBusiness implements GoatAbccImportUseCase {

    private static final Set<String> ABCC_DEATH_SITUATIONS = Set.of(
            "FALECIDO", "FALECIDA", "MORTO", "MORTA", "OBITO", "DECEASED"
    );

    private static final String STATUS_IMPORTED = "IMPORTED";
    private static final String STATUS_SKIPPED_DUPLICATE = "SKIPPED_DUPLICATE";
    private static final String STATUS_SKIPPED_TOD_MISMATCH = "SKIPPED_TOD_MISMATCH";
    private static final String STATUS_ERROR = "ERROR";

    private final FarmAuthorizationUseCase ownershipService;
    private final GoatFarmPersistencePort goatFarmPort;
    private final GoatAbccQueryUseCase goatAbccQueryUseCase;
    private final GoatManagementUseCase goatManagementUseCase;
    private final GoatReferenceQueryPort goatReferenceQueryPort;
    private final EntityFinder entityFinder;
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;
    private final AbccAnimalTranslator animalTranslator;
    private final AbccImportEligibilityPolicy eligibilityPolicy;

    public GoatAbccImportBusiness(
            FarmAuthorizationUseCase ownershipService,
            GoatFarmPersistencePort goatFarmPort,
            GoatAbccQueryUseCase goatAbccQueryUseCase,
            GoatManagementUseCase goatManagementUseCase,
            GoatReferenceQueryPort goatReferenceQueryPort,
            EntityFinder entityFinder,
            CurrentPrincipalQueryUseCase currentPrincipalQuery,
            AbccAnimalTranslator animalTranslator,
            AbccImportEligibilityPolicy eligibilityPolicy
    ) {
        this.ownershipService = ownershipService;
        this.goatFarmPort = goatFarmPort;
        this.goatAbccQueryUseCase = goatAbccQueryUseCase;
        this.goatManagementUseCase = goatManagementUseCase;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.entityFinder = entityFinder;
        this.currentPrincipalQuery = currentPrincipalQuery;
        this.animalTranslator = animalTranslator;
        this.eligibilityPolicy = eligibilityPolicy;
    }

    @Override
    public GoatResponseVO confirm(Long farmId, String externalId, GoatRequestVO goatRequestVO) {
        ownershipService.verifyFarmOwnership(farmId);

        if (isBlank(externalId)) {
            throw new BusinessRuleException("externalId", "Identificador externo da ABCC é obrigatório.");
        }
        if (goatRequestVO == null) {
            throw new BusinessRuleException("goat", "Dados do animal são obrigatórios para confirmar a importação.");
        }
        requireLocalStatus(goatRequestVO.getStatus());

        boolean isAdmin = currentPrincipalQuery.requireCurrent().hasAuthority("ROLE_ADMIN");
        FarmRecord farm = loadFarm(farmId);
        String farmTod = eligibilityPolicy.requireFarmTodForImport(farm, isAdmin);

        GoatAbccPreviewResponseVO abccPreview = goatAbccQueryUseCase.preview(
                farmId,
                GoatAbccPreviewRequestVO.builder().externalId(externalId).build()
        );
        goatRequestVO.setStatus(resolveImportStatus(abccPreview, goatRequestVO.getStatus()));
        return confirmFromPreview(farmId, externalId, goatRequestVO, abccPreview, isAdmin, farmTod);
    }

    @Override
    public GoatAbccBatchConfirmResponseVO confirmBatch(Long farmId, List<GoatAbccBatchConfirmItemVO> items) {
        ownershipService.verifyFarmOwnership(farmId);

        if (items == null || items.isEmpty()) {
            throw new BusinessRuleException("items", "Selecione ao menos um animal da página atual para importar.");
        }
        requireBatchLocalStatuses(items);

        boolean isAdmin = currentPrincipalQuery.requireCurrent().hasAuthority("ROLE_ADMIN");
        FarmRecord farm = loadFarm(farmId);
        eligibilityPolicy.requireFarmTodForImport(farm, isAdmin);

        List<GoatAbccBatchConfirmItemResultVO> results = new ArrayList<>();
        int imported = 0;
        int skippedDuplicate = 0;
        int skippedTodMismatch = 0;
        int error = 0;

        for (int index = 0; index < items.size(); index++) {
            GoatAbccBatchConfirmItemVO item = items.get(index);
            String externalId = item == null ? null : trimOrNull(item.getExternalId());
            if (externalId == null) {
                error++;
                results.add(GoatAbccBatchConfirmItemResultVO.builder()
                        .status(STATUS_ERROR)
                        .message("Item " + (index + 1) + " sem identificador externo válido.")
                        .build());
                continue;
            }

            try {
                GoatAbccPreviewResponseVO previewVO = goatAbccQueryUseCase.preview(
                        farmId,
                        GoatAbccPreviewRequestVO.builder().externalId(externalId).build()
                );
                GoatRequestVO goatRequestVO = animalTranslator.buildGoatRequestFromPreview(
                        previewVO,
                        resolveImportStatus(previewVO, item.getStatus())
                );
                String registrationNumber = goatRequestVO.getRegistrationNumber();

                if (goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId).isPresent()) {
                    skippedDuplicate++;
                    results.add(GoatAbccBatchConfirmItemResultVO.builder()
                            .externalId(externalId)
                            .registrationNumber(registrationNumber)
                            .name(goatRequestVO.getName())
                            .status(STATUS_SKIPPED_DUPLICATE)
                            .message("Registro já existente nesta fazenda. Item ignorado por duplicidade.")
                            .build());
                    continue;
                }

                GoatResponseVO created = confirmFromPreview(
                        farmId,
                        externalId,
                        goatRequestVO,
                        previewVO,
                        isAdmin,
                        trimOrNull(farm.tod())
                );
                imported++;
                results.add(GoatAbccBatchConfirmItemResultVO.builder()
                        .externalId(externalId)
                        .registrationNumber(created.getRegistrationNumber())
                        .name(created.getName())
                        .status(STATUS_IMPORTED)
                        .message("Animal importado com sucesso.")
                        .build());
            } catch (BusinessRuleException ex) {
                if (eligibilityPolicy.isAbccTodMismatch(ex)) {
                    skippedTodMismatch++;
                    results.add(GoatAbccBatchConfirmItemResultVO.builder()
                            .externalId(externalId)
                            .status(STATUS_SKIPPED_TOD_MISMATCH)
                            .message(ex.getMessage())
                            .build());
                    continue;
                }

                error++;
                results.add(GoatAbccBatchConfirmItemResultVO.builder()
                        .externalId(externalId)
                        .status(STATUS_ERROR)
                        .message(ex.getMessage())
                        .build());
            } catch (DuplicateEntityException ex) {
                error++;
                results.add(GoatAbccBatchConfirmItemResultVO.builder()
                        .externalId(externalId)
                        .status(STATUS_ERROR)
                        .message("Conflito de registro durante a importação: " + ex.getMessage())
                        .build());
            } catch (RuntimeException ex) {
                error++;
                results.add(GoatAbccBatchConfirmItemResultVO.builder()
                        .externalId(externalId)
                        .status(STATUS_ERROR)
                        .message("Falha ao importar o item selecionado.")
                        .build());
            }
        }

        return GoatAbccBatchConfirmResponseVO.builder()
                .totalSelected(items.size())
                .totalImported(imported)
                .totalSkippedDuplicate(skippedDuplicate)
                .totalSkippedTodMismatch(skippedTodMismatch)
                .totalError(error)
                .results(results)
                .build();
    }

    private GoatResponseVO confirmFromPreview(
            Long farmId,
            String externalId,
            GoatRequestVO goatRequestVO,
            GoatAbccPreviewResponseVO abccPreview,
            boolean isAdmin,
            String farmTod
    ) {
        eligibilityPolicy.validatePreviewTod(isAdmin, farmTod, abccPreview.getTod());
        eligibilityPolicy.validateRequestTod(isAdmin, farmTod, goatRequestVO.getTod());

        goatRequestVO.setCreatorProvenance(GoatCreatorProvenanceVO.builder()
                .creatorNameSnapshot(trimOrNull(abccPreview.getCreatorName()))
                .creatorTod(trimOrNull(abccPreview.getTod()))
                .evidenceReference("ABCC:" + externalId.trim())
                .build());
        return goatManagementUseCase.createGoat(farmId, goatRequestVO, GoatCreationOrigin.ABCC_IMPORT);
    }

    private FarmRecord loadFarm(Long farmId) {
        return entityFinder.findOrThrow(
                () -> goatFarmPort.findById(farmId),
                "Fazenda não encontrada."
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void requireLocalStatus(GoatStatus status) {
        if (status == null) {
            throw new BusinessRuleException("status", "A situação local do animal é obrigatória para confirmar a importação.");
        }
    }

    private void requireBatchLocalStatuses(List<GoatAbccBatchConfirmItemVO> items) {
        for (int index = 0; index < items.size(); index++) {
            GoatAbccBatchConfirmItemVO item = items.get(index);
            if (item == null || item.getStatus() == null) {
                throw new BusinessRuleException(
                        "items[" + index + "].status",
                        "A situação local é obrigatória para cada animal selecionado."
                );
            }
        }
    }

    private GoatStatus resolveImportStatus(GoatAbccPreviewResponseVO freshPreview, GoatStatus requestedLocalStatus) {
        if (isAbccReportedDeceased(freshPreview.getAbccSituation())) {
            return GoatStatus.FALECIDO;
        }
        return requestedLocalStatus;
    }

    private boolean isAbccReportedDeceased(String abccSituation) {
        if (abccSituation == null || abccSituation.isBlank()) {
            return false;
        }
        String normalized = Normalizer.normalize(abccSituation, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toUpperCase(Locale.ROOT);
        return ABCC_DEATH_SITUATIONS.contains(normalized);
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
