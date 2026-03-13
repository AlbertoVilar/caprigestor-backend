package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccImportUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
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
    private static final DateTimeFormatter ABCC_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final OwnershipService ownershipService;
    private final GoatFarmPersistencePort goatFarmPort;
    private final GoatAbccPublicQueryPort abccPublicQueryPort;
    private final GoatManagementUseCase goatManagementUseCase;
    private final EntityFinder entityFinder;

    public GoatAbccImportBusiness(
            OwnershipService ownershipService,
            GoatFarmPersistencePort goatFarmPort,
            GoatAbccPublicQueryPort abccPublicQueryPort,
            GoatManagementUseCase goatManagementUseCase,
            EntityFinder entityFinder
    ) {
        this.ownershipService = ownershipService;
        this.goatFarmPort = goatFarmPort;
        this.abccPublicQueryPort = abccPublicQueryPort;
        this.goatManagementUseCase = goatManagementUseCase;
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

        Integer resolvedRaceId = resolveRaceId(requestVO);
        GoatAbccSearchRequestVO normalizedRequest = GoatAbccSearchRequestVO.builder()
                .raceId(resolvedRaceId)
                .raceName(requestVO.getRaceName())
                .affix(requestVO.getAffix())
                .page(requestVO.getPage())
                .sex(requestVO.getSex())
                .tod(requestVO.getTod())
                .toe(requestVO.getToe())
                .name(requestVO.getName())
                .dna(requestVO.getDna())
                .build();

        GoatAbccRawSearchResultVO rawResult;
        try {
            rawResult = abccPublicQueryPort.search(normalizedRequest);
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", "Não foi possível consultar a ABCC pública no momento.");
        }

        List<GoatAbccSearchItemVO> normalizedItems = rawResult.getItems() == null
                ? List.of()
                : rawResult.getItems().stream().map(this::normalizeSearchItem).toList();

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

        GoatAbccRawPreviewVO raw;
        try {
            raw = abccPublicQueryPort.preview(requestVO.getExternalId());
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", "Não foi possível obter o preview do animal na ABCC pública.");
        }

        GoatFarm farm = entityFinder.findOrThrow(
                () -> goatFarmPort.findById(farmId),
                "Fazenda não encontrada."
        );
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
                .tod(trimOrNull(raw.getTod()))
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
        if (isBlank(externalId)) {
            throw new BusinessRuleException("externalId", "Identificador externo da ABCC é obrigatório.");
        }
        if (goatRequestVO == null) {
            throw new BusinessRuleException("goat", "Dados do animal são obrigatórios para confirmar a importação.");
        }
        return goatManagementUseCase.createGoat(farmId, goatRequestVO);
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
            case "RGD", "ATIVO", "ATIVA", "REGISTRADO", "REGISTRO DEFINITIVO" -> GoatStatus.ATIVO;
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
