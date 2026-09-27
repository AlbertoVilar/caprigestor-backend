package com.devmaster.goatfarm.goat.business.abcc;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchItemVO;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Anti-corruption layer for the external ABCC vocabulary and representation.
 *
 * <p>This collaborator is intentionally stateless. Import authorization,
 * ownership, provenance and orchestration remain in the application service.</p>
 */
@Component
public class AbccAnimalTranslator {

    private static final String ABCC_SOURCE = "ABCC_PUBLIC";
    private static final DateTimeFormatter ABCC_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public GoatAbccRaceOptionVO toNormalizedRaceOption(GoatAbccRaceOptionVO option) {
        return GoatAbccRaceOptionVO.builder()
                .id(option.getId())
                .name(trimOrNull(option.getName()))
                .normalizedBreed(normalizeBreedInternal(option.getName()))
                .build();
    }

    public GoatAbccSearchItemVO normalizeSearchItem(GoatAbccRawSearchItemVO raw) {
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

    public GoatAbccPreviewResponseVO toPreview(GoatAbccRawPreviewVO raw, Long farmId, String farmName) {
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
                .creatorName(trimOrNull(raw.getCriador()))
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
                .userName(null)
                .farmId(farmId)
                .farmName(farmName)
                .normalizationWarnings(warnings)
                .build();
    }

    public GoatRequestVO buildGoatRequestFromPreview(GoatAbccPreviewResponseVO previewVO) {
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
            throw new BusinessRuleException("tod", "TOD ABCC ausente para importar este item.");
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

    public String normalizeRegistrationForLookup(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    public String normalizedToken(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    public GoatBreed normalizeBreedInternal(String value) {
        if (isBlank(value)) {
            return null;
        }

        return switch (normalizedToken(value)) {
            case "ALPINA", "ALPINA FRANCESA" -> GoatBreed.ALPINA;
            case "ALPINA AMERICANA" -> GoatBreed.ALPINA_AMERICANA;
            case "ALPINA BRITANICA" -> GoatBreed.ALPINA_BRITANICA;
            case "ALPINE" -> GoatBreed.ALPINA;
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

    public String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    private GoatStatus normalizeStatus(String value, List<String> warnings, String fieldLabel) {
        if (isBlank(value)) {
            return null;
        }
        return switch (normalizedToken(value)) {
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
        return switch (normalizedToken(value)) {
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
