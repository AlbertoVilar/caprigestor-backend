package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccImportUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchResponseVO;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GoatAbccImportBusiness implements GoatAbccImportUseCase {

    private static final String ABCC_SOURCE = "ABCC_PUBLIC";
    private static final String STATUS_IMPORTED = "IMPORTED";
    private static final String STATUS_SKIPPED_DUPLICATE = "SKIPPED_DUPLICATE";
    private static final String STATUS_SKIPPED_TOD_MISMATCH = "SKIPPED_TOD_MISMATCH";
    private static final String STATUS_ERROR = "ERROR";

    private static final String FIELD_TOD = "tod";
    private static final String MSG_ABCC_UNAVAILABLE = "Não foi possível consultar a ABCC pública no momento.";
    private static final String MSG_PREVIEW_UNAVAILABLE = "Não foi possível obter o preview do animal na ABCC pública.";
    private static final String MSG_MISSING_FARM_TOD = "A fazenda não possui TOD configurado. Configure o TOD da fazenda para usar a importação ABCC.";
    private static final String MSG_TOD_MISMATCH = "O animal selecionado possui TOD diferente do TOD da fazenda. Importação ABCC permitida apenas para animais do mesmo TOD.";
    private static final String MSG_REQUEST_TOD_MISMATCH = "Para importar pela ABCC, o TOD informado deve ser igual ao TOD da fazenda.";

    private static final DateTimeFormatter ABCC_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final OwnershipService ownershipService;
    private final GoatFarmPersistencePort goatFarmPort;
    private final GoatAbccPublicQueryPort abccPublicQueryPort;
    private final GoatManagementUseCase goatManagementUseCase;
    private final GoatPersistencePort goatPersistencePort;
    private final EntityFinder entityFinder;

    public GoatAbccImportBusiness(
            OwnershipService ownershipService,
            GoatFarmPersistencePort goatFarmPort,
            GoatAbccPublicQueryPort abccPublicQueryPort,
            GoatManagementUseCase goatManagementUseCase,
            GoatPersistencePort goatPersistencePort,
            EntityFinder entityFinder
    ) {
        this.ownershipService = ownershipService;
        this.goatFarmPort = goatFarmPort;
        this.abccPublicQueryPort = abccPublicQueryPort;
        this.goatManagementUseCase = goatManagementUseCase;
        this.goatPersistencePort = goatPersistencePort;
        this.entityFinder = entityFinder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoatAbccRaceOptionVO> listRaces(Long farmId) {
        ownershipService.verifyFarmOwnership(farmId);

        List<GoatAbccRaceOptionVO> raceOptions = fetchAbccRaceCatalog();
        return raceOptions.stream()
                .map(this::toNormalizedRaceOption)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GoatAbccSearchResponseVO search(Long farmId, GoatAbccSearchRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validateSearchRequest(requestVO);

        boolean isAdmin = ownershipService.isCurrentUserAdmin();
        GoatFarm farm = loadFarm(farmId);
        String farmTod = requireFarmTodForNonAdmin(farm, isAdmin);

        Integer resolvedRaceId = resolveRaceId(requestVO);
        GoatAbccSearchRequestVO normalizedRequest = GoatAbccSearchRequestVO.builder()
                .raceId(resolvedRaceId)
                .raceName(requestVO.getRaceName())
                .affix(requestVO.getAffix())
                .page(requestVO.getPage())
                .sex(requestVO.getSex())
                .tod(isAdmin ? requestVO.getTod() : farmTod)
                .toe(requestVO.getToe())
                .name(requestVO.getName())
                .dna(requestVO.getDna())
                .build();

        GoatAbccRawSearchResultVO rawResult;
        try {
            rawResult = abccPublicQueryPort.search(normalizedRequest);
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", MSG_ABCC_UNAVAILABLE);
        }

        List<GoatAbccSearchItemVO> normalizedItems = rawResult.getItems() == null
                ? List.of()
                : rawResult.getItems().stream()
                .map(this::normalizeSearchItem)
                .filter(item -> isAdmin || isSameTod(item.getTod(), farmTod))
                .toList();

        return GoatAbccSearchResponseVO.builder()
                .currentPage(rawResult.getCurrentPage())
                .totalPages(rawResult.getTotalPages())
                .pageSize(normalizedItems.size())
                .items(normalizedItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GoatAbccPreviewResponseVO preview(Long farmId, GoatAbccPreviewRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        if (requestVO == null || isBlank(requestVO.getExternalId())) {
            throw new BusinessRuleException("externalId", "Identificador externo da ABCC é obrigatório.");
        }

        boolean isAdmin = ownershipService.isCurrentUserAdmin();
        GoatFarm farm = loadFarm(farmId);
        String farmTod = requireFarmTodForNonAdmin(farm, isAdmin);

        GoatAbccRawPreviewVO raw;
        try {
            raw = abccPublicQueryPort.preview(requestVO.getExternalId());
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", MSG_PREVIEW_UNAVAILABLE);
        }

        String abccTod = trimOrNull(raw.getTod());
        enforceTodMatchForNonAdmin(isAdmin, farmTod, abccTod);

        User currentUser = ownershipService.getCurrentUser();

        List<String> warnings = new ArrayList<>();
        Gender gender = normalizeGender(raw.getSexo(), warnings, "sexo");
        GoatBreed breed = normalizeBreed(raw.getRaca(), warnings, "raça");
        GoatStatus status = normalizeStatus(raw.getSituacao(), warnings, "situação");
        Category category = normalizeCategory(raw.getCategoria(), warnings, "categoria");
        LocalDate birthDate = parseDate(raw.getDataNascimento(), warnings, "dataNascimento");

        if (isBlank(raw.getRegistro())) {
            warnings.add("Registro ABCC não informado no preview.");
        }
        if (isBlank(raw.getNome())) {
            warnings.add("Nome do animal não informado no preview.");
        }

        return GoatAbccPreviewResponseVO.builder()
                .externalSource(ABCC_SOURCE)
                .externalId(raw.getExternalId())
                .registrationNumber(trimOrNull(raw.getRegistro()))
                .name(trimOrNull(raw.getNome()))
                .gender(gender)
                .breed(breed)
                .color(trimOrNull(raw.getPelagem()))
                .birthDate(birthDate)
                .status(status)
                .tod(abccTod)
                .toe(trimOrNull(raw.getToe()))
                .category(category)
                .fatherName(trimOrNull(raw.getPaiNome()))
                .fatherRegistrationNumber(trimOrNull(raw.getPaiRegistro()))
                .motherName(trimOrNull(raw.getMaeNome()))
                .motherRegistrationNumber(trimOrNull(raw.getMaeRegistro()))
                .userName(currentUser != null ? currentUser.getName() : null)
                .farmId(farmId)
                .farmName(farm.getName())
                .normalizationWarnings(warnings)
                .build();
    }

    @Override
    @Transactional
    public GoatResponseVO confirm(Long farmId, String externalId, GoatRequestVO goatRequestVO) {
        ownershipService.verifyFarmOwnership(farmId);

        if (isBlank(externalId)) {
            throw new BusinessRuleException("externalId", "Identificador externo da ABCC é obrigatório.");
        }
        if (goatRequestVO == null) {
            throw new BusinessRuleException("goat", "Dados do animal são obrigatórios para confirmar a importação.");
        }

        boolean isAdmin = ownershipService.isCurrentUserAdmin();
        GoatFarm farm = loadFarm(farmId);
        String farmTod = requireFarmTodForNonAdmin(farm, isAdmin);

        GoatAbccPreviewResponseVO abccPreview = preview(
                farmId,
                GoatAbccPreviewRequestVO.builder().externalId(externalId).build()
        );
        enforceTodMatchForNonAdmin(isAdmin, farmTod, trimOrNull(abccPreview.getTod()));

        if (!isAdmin && !isSameTod(goatRequestVO.getTod(), farmTod)) {
            throw new BusinessRuleException(FIELD_TOD, MSG_REQUEST_TOD_MISMATCH);
        }

        return goatManagementUseCase.createGoat(farmId, goatRequestVO);
    }

    @Override
    @Transactional
    public GoatAbccBatchConfirmResponseVO confirmBatch(Long farmId, List<GoatAbccBatchConfirmItemVO> items) {
        ownershipService.verifyFarmOwnership(farmId);

        if (items == null || items.isEmpty()) {
            throw new BusinessRuleException("items", "Selecione ao menos um animal da página atual para importar.");
        }

        boolean isAdmin = ownershipService.isCurrentUserAdmin();
        GoatFarm farm = loadFarm(farmId);
        requireFarmTodForNonAdmin(farm, isAdmin);

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
                GoatAbccPreviewResponseVO previewVO = preview(
                        farmId,
                        GoatAbccPreviewRequestVO.builder().externalId(externalId).build()
                );
                GoatRequestVO goatRequestVO = buildGoatRequestFromPreview(previewVO);
                String registrationNumber = goatRequestVO.getRegistrationNumber();

                if (goatPersistencePort.findByIdAndFarmId(registrationNumber, farmId).isPresent()) {
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

                GoatResponseVO created = confirm(farmId, externalId, goatRequestVO);
                imported++;
                results.add(GoatAbccBatchConfirmItemResultVO.builder()
                        .externalId(externalId)
                        .registrationNumber(created.getRegistrationNumber())
                        .name(created.getName())
                        .status(STATUS_IMPORTED)
                        .message("Animal importado com sucesso.")
                        .build());
            } catch (BusinessRuleException ex) {
                if (isTodMismatchException(ex)) {
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

    private GoatFarm loadFarm(Long farmId) {
        return entityFinder.findOrThrow(
                () -> goatFarmPort.findById(farmId),
                "Fazenda não encontrada."
        );
    }

    private String requireFarmTodForNonAdmin(GoatFarm farm, boolean isAdmin) {
        if (isAdmin) {
            return null;
        }

        String farmTod = trimOrNull(farm.getTod());
        if (farmTod == null) {
            throw new BusinessRuleException(FIELD_TOD, MSG_MISSING_FARM_TOD);
        }
        return farmTod;
    }

    private void enforceTodMatchForNonAdmin(boolean isAdmin, String farmTod, String abccTod) {
        if (isAdmin) {
            return;
        }
        if (!isSameTod(abccTod, farmTod)) {
            throw new BusinessRuleException(FIELD_TOD, MSG_TOD_MISMATCH);
        }
    }

    private boolean isTodMismatchException(BusinessRuleException ex) {
        return FIELD_TOD.equals(ex.getFieldName()) && MSG_TOD_MISMATCH.equals(ex.getMessage());
    }

    private boolean isSameTod(String left, String right) {
        String normalizedLeft = trimOrNull(left);
        String normalizedRight = trimOrNull(right);
        if (normalizedLeft == null || normalizedRight == null) {
            return false;
        }
        return normalizedLeft.equalsIgnoreCase(normalizedRight);
    }

    private GoatRequestVO buildGoatRequestFromPreview(GoatAbccPreviewResponseVO previewVO) {
        String registrationNumber = trimOrNull(previewVO.getRegistrationNumber());
        String name = trimOrNull(previewVO.getName());
        String color = trimOrNull(previewVO.getColor());
        String tod = trimOrNull(previewVO.getTod());
        String toe = trimOrNull(previewVO.getToe());

        if (registrationNumber == null) {
            throw new BusinessRuleException("registrationNumber", "Registro ABCC ausente para importar este item.");
        }
        if (name == null) {
            throw new BusinessRuleException("name", "Nome ABCC ausente para importar este item.");
        }
        if (previewVO.getGender() == null) {
            throw new BusinessRuleException("gender", "Sexo ABCC não mapeado para importar este item.");
        }
        if (previewVO.getBreed() == null) {
            throw new BusinessRuleException("breed", "Raça ABCC não mapeada para importar este item.");
        }
        if (color == null) {
            throw new BusinessRuleException("color", "Pelagem ABCC ausente para importar este item.");
        }
        if (previewVO.getBirthDate() == null) {
            throw new BusinessRuleException("birthDate", "Data de nascimento ABCC inválida para importar este item.");
        }
        if (previewVO.getStatus() == null) {
            throw new BusinessRuleException("status", "Situação ABCC não mapeada para importar este item.");
        }
        if (tod == null) {
            throw new BusinessRuleException(FIELD_TOD, "TOD ABCC ausente para importar este item.");
        }
        if (toe == null) {
            throw new BusinessRuleException("toe", "TOE ABCC ausente para importar este item.");
        }

        return GoatRequestVO.builder()
                .registrationNumber(registrationNumber)
                .name(name)
                .gender(previewVO.getGender())
                .breed(previewVO.getBreed())
                .color(color)
                .birthDate(previewVO.getBirthDate())
                .status(previewVO.getStatus())
                .tod(tod)
                .toe(toe)
                .category(previewVO.getCategory())
                .fatherRegistrationNumber(trimOrNull(previewVO.getFatherRegistrationNumber()))
                .motherRegistrationNumber(trimOrNull(previewVO.getMotherRegistrationNumber()))
                .build();
    }

    private void validateSearchRequest(GoatAbccSearchRequestVO requestVO) {
        if (requestVO == null) {
            throw new BusinessRuleException("payload", "Payload de busca ABCC é obrigatório.");
        }
        if (requestVO.getRaceId() == null && isBlank(requestVO.getRaceName())) {
            throw new BusinessRuleException("raceName", "Raça ABCC é obrigatória.");
        }
        if (isBlank(requestVO.getAffix())) {
            throw new BusinessRuleException("affix", "Afixo é obrigatório para busca na ABCC.");
        }
        if (requestVO.getPage() != null && requestVO.getPage() < 1) {
            throw new BusinessRuleException("page", "Página deve ser maior ou igual a 1.");
        }
    }

    private Integer resolveRaceId(GoatAbccSearchRequestVO requestVO) {
        if (requestVO.getRaceId() != null && requestVO.getRaceId() > 0) {
            return requestVO.getRaceId();
        }

        String requestedRaceName = trimOrNull(requestVO.getRaceName());
        if (requestedRaceName == null) {
            throw new BusinessRuleException("raceName", "Raça ABCC é obrigatória.");
        }

        List<GoatAbccRaceOptionVO> raceOptions = fetchAbccRaceCatalog();
        String requestedToken = normalizedToken(requestedRaceName);

        return raceOptions.stream()
                .filter(option -> normalizedToken(option.getName()).equals(requestedToken))
                .map(GoatAbccRaceOptionVO::getId)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        "raceName",
                        "Raça ABCC inválida. Consulte a lista de raças disponíveis antes de buscar."
                ));
    }

    private List<GoatAbccRaceOptionVO> fetchAbccRaceCatalog() {
        try {
            List<GoatAbccRaceOptionVO> raceOptions = abccPublicQueryPort.listRaces();
            if (raceOptions == null || raceOptions.isEmpty()) {
                throw new BusinessRuleException("abcc", "Não foi possível carregar a lista de raças da ABCC.");
            }
            return raceOptions;
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", "Não foi possível carregar a lista de raças da ABCC pública.");
        }
    }

    private GoatAbccRaceOptionVO toNormalizedRaceOption(GoatAbccRaceOptionVO option) {
        return GoatAbccRaceOptionVO.builder()
                .id(option.getId())
                .name(trimOrNull(option.getName()))
                .normalizedBreed(normalizeBreedInternal(option.getName()))
                .build();
    }

    private GoatAbccSearchItemVO normalizeSearchItem(GoatAbccRawSearchItemVO raw) {
        List<String> warnings = new ArrayList<>();
        Gender gender = normalizeGender(raw.getSexo(), warnings, "sexo");
        GoatBreed breed = normalizeBreed(raw.getRaca(), warnings, "raça");
        GoatStatus status = normalizeStatus(raw.getSituacao(), warnings, "situação");

        return GoatAbccSearchItemVO.builder()
                .externalSource(ABCC_SOURCE)
                .externalId(trimOrNull(raw.getExternalId()))
                .nome(trimOrNull(raw.getNome()))
                .situacao(trimOrNull(raw.getSituacao()))
                .dna(trimOrNull(raw.getDna()))
                .tod(trimOrNull(raw.getTod()))
                .toe(trimOrNull(raw.getToe()))
                .criador(trimOrNull(raw.getCriador()))
                .afixo(trimOrNull(raw.getAfixo()))
                .dataNascimento(trimOrNull(raw.getDataNascimento()))
                .sexo(trimOrNull(raw.getSexo()))
                .raca(trimOrNull(raw.getRaca()))
                .pelagem(trimOrNull(raw.getPelagem()))
                .normalizedGender(gender)
                .normalizedBreed(breed)
                .normalizedStatus(status)
                .normalizationWarnings(warnings)
                .build();
    }

    private Gender normalizeGender(String value, List<String> warnings, String fieldLabel) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Gender.fromValue(value);
        } catch (RuntimeException ex) {
            warnings.add("Valor de " + fieldLabel + " da ABCC não mapeado: " + value);
            return null;
        }
    }

    private GoatBreed normalizeBreed(String value, List<String> warnings, String fieldLabel) {
        if (isBlank(value)) {
            return null;
        }

        GoatBreed mapped = normalizeBreedInternal(value);
        if (mapped != null) {
            return mapped;
        }

        warnings.add("Valor de " + fieldLabel + " da ABCC não mapeado: " + value);
        return null;
    }

    private GoatBreed normalizeBreedInternal(String value) {
        if (isBlank(value)) {
            return null;
        }

        String token = normalizedToken(value);
        return switch (token) {
            case "ALPINA", "ALPINA FRANCESA" -> GoatBreed.ALPINA;
            case "ALPINA AMERICANA" -> GoatBreed.ALPINA_AMERICANA;
            case "ALPINA BRITANICA" -> GoatBreed.ALPINA_BRITANICA;
            case "ALPINE" -> GoatBreed.ALPINE;
            case "ANGLONUBIANA", "ANGLO NUBIANA", "ANGLO-NUBIANA" -> GoatBreed.ANGLO_NUBIANA;
            case "ANGORA" -> GoatBreed.ANGORA;
            case "BHUJ" -> GoatBreed.BHUJ;
            case "BOER" -> GoatBreed.BOER;
            case "CANINDE" -> GoatBreed.CANINDE;
            case "JAMNAPARI" -> GoatBreed.JAMNAPARI;
            case "KALAHARI" -> GoatBreed.KALAHARI;
            case "MAMBRINA" -> GoatBreed.MAMBRINA;
            case "MESTICA", "MESTICAO", "MESTIÇA" -> GoatBreed.MESTICA;
            case "MOXOTO" -> GoatBreed.MOXOTO;
            case "MURCIANA" -> GoatBreed.MURCIANA;
            case "MURCIANA GRANADINA" -> GoatBreed.MURCIANA_GRANADINA;
            case "SAANEN" -> GoatBreed.SAANEN;
            case "SAVANA" -> GoatBreed.SAVANA;
            case "SRD" -> GoatBreed.SRD;
            case "TOGGENBURG" -> GoatBreed.TOGGENBURG;
            default -> null;
        };
    }

    private GoatStatus normalizeStatus(String value, List<String> warnings, String fieldLabel) {
        if (isBlank(value)) {
            return null;
        }
        String token = normalizedToken(value);
        return switch (token) {
            case "RGD", "SEM RGD", "SEM R.G.D.", "ATIVO", "ATIVA", "REGISTRADO", "REGISTRO DEFINITIVO" -> GoatStatus.ATIVO;
            case "INATIVO", "INATIVA", "SUSPENSO", "SUSPENSA" -> GoatStatus.INATIVO;
            case "VENDIDO", "VENDIDA", "ALIENADO", "ALIENADA" -> GoatStatus.VENDIDO;
            case "FALECIDO", "FALECIDA", "OBITO", "MORTO", "MORTA" -> GoatStatus.FALECIDO;
            default -> {
                warnings.add("Valor de " + fieldLabel + " da ABCC não mapeado: " + value);
                yield null;
            }
        };
    }

    private Category normalizeCategory(String value, List<String> warnings, String fieldLabel) {
        if (isBlank(value)) {
            return null;
        }
        String token = normalizedToken(value);
        return switch (token) {
            case "PO", "PURO DE ORIGEM" -> Category.PO;
            case "PA", "PURO POR AVALIACAO" -> Category.PA;
            case "PC", "PURO POR CRUZA" -> Category.PC;
            case "PCOD" -> {
                warnings.add("Categoria ABCC PCOD mapeada para PC por compatibilidade.");
                yield Category.PC;
            }
            default -> {
                warnings.add("Valor de " + fieldLabel + " da ABCC não mapeado: " + value);
                yield null;
            }
        };
    }

    private LocalDate parseDate(String value, List<String> warnings, String fieldLabel) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), ABCC_DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            warnings.add("Valor de " + fieldLabel + " da ABCC inválido: " + value);
            return null;
        }
    }

    private String normalizedToken(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
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
