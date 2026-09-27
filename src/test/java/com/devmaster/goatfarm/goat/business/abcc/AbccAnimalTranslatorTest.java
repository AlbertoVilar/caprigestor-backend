package com.devmaster.goatfarm.goat.business.abcc;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbccAnimalTranslatorTest {

    private final AbccAnimalTranslator translator = new AbccAnimalTranslator();

    @Test
    void mapsGenderBreedStatusAndCategoryVocabulary() {
        GoatAbccPreviewResponseVO preview = translator.toPreview(rawPreview(
                "Macho", "ALPINA FRANCESA", "Sem RGD", "PCOD", "10/01/2020"), 7L, "Capril Vilar");

        assertThat(preview.getGender()).isEqualTo(Gender.MACHO);
        assertThat(preview.getBreed()).isEqualTo(GoatBreed.ALPINA);
        assertThat(preview.getStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(preview.getCategory()).isEqualTo(Category.PC);
        assertThat(preview.getBirthDate()).isEqualTo(LocalDate.of(2020, 1, 10));
        assertThat(preview.getNormalizationWarnings())
                .containsExactly("Categoria ABCC PCOD mapeada para PC por compatibilidade.");
    }

    @Test
    void mapsRepresentativeBreedAliasesAndAccentNormalization() {
        assertThat(translator.normalizeBreedInternal("ALPINE")).isEqualTo(GoatBreed.ALPINA);
        assertThat(translator.normalizeBreedInternal("ANGLO-NUBIANA")).isEqualTo(GoatBreed.ANGLO_NUBIANA);
        assertThat(translator.normalizeBreedInternal("Mestiça")).isEqualTo(GoatBreed.MESTICA);
        assertThat(translator.normalizeBreedInternal("SAANEN")).isEqualTo(GoatBreed.SAANEN);
    }

    @Test
    void mapsAllKnownStatusValuesAndWarnsForUnknown() {
        assertThat(translator.toPreview(rawPreview("Fêmea", "SAANEN", "ATIVA", "PO", "10/01/2020"), 1L, "Fazenda").getStatus())
                .isEqualTo(GoatStatus.ATIVO);
        assertThat(translator.toPreview(rawPreview("Fêmea", "SAANEN", "INATIVO", "PO", "10/01/2020"), 1L, "Fazenda").getStatus())
                .isEqualTo(GoatStatus.INATIVO);
        assertThat(translator.toPreview(rawPreview("Fêmea", "SAANEN", "VENDIDO", "PO", "10/01/2020"), 1L, "Fazenda").getStatus())
                .isEqualTo(GoatStatus.VENDIDO);
        assertThat(translator.toPreview(rawPreview("Fêmea", "SAANEN", "FALECIDA", "PO", "10/01/2020"), 1L, "Fazenda").getStatus())
                .isEqualTo(GoatStatus.FALECIDO);

        GoatAbccPreviewResponseVO unknown = translator.toPreview(
                rawPreview("Fêmea", "SAANEN", "NOVA SITUAÇÃO", "PO", "10/01/2020"), 1L, "Fazenda");
        assertThat(unknown.getStatus()).isNull();
        assertThat(unknown.getNormalizationWarnings()).contains("Valor de situação da ABCC não mapeado: NOVA SITUAÇÃO");
    }

    @Test
    void warnsForUnknownGenderBreedAndCategory() {
        GoatAbccPreviewResponseVO preview = translator.toPreview(rawPreview(
                "OUTRO", "RAÇA NOVA", "RGD", "CATEGORIA NOVA", "10/01/2020"), 1L, "Fazenda");

        assertThat(preview.getGender()).isNull();
        assertThat(preview.getBreed()).isNull();
        assertThat(preview.getCategory()).isNull();
        assertThat(preview.getNormalizationWarnings())
                .contains("Valor de sexo da ABCC não mapeado: OUTRO")
                .contains("Valor de raça da ABCC não mapeado: RAÇA NOVA")
                .contains("Valor de categoria da ABCC não mapeado: CATEGORIA NOVA");
    }

    @Test
    void preservesDateAndBlankNormalizationBehavior() {
        GoatAbccPreviewResponseVO malformed = translator.toPreview(
                rawPreview("Macho", "SAANEN", "RGD", "PO", "31/02/2020"), 1L, "Fazenda");
        assertThat(malformed.getBirthDate()).isNull();
        assertThat(malformed.getNormalizationWarnings()).contains("Valor de dataNascimento da ABCC inválido: 31/02/2020");

        GoatAbccPreviewResponseVO blank = translator.toPreview(
                rawPreview(" ", " ", " ", " ", " "), 1L, "Fazenda");
        blank.setName(null);
        blank.setRegistrationNumber(null);
        assertThat(blank.getBirthDate()).isNull();
        assertThat(blank.getName()).isNull();
        assertThat(blank.getRegistrationNumber()).isNull();
        assertThat(translator.normalizeRegistrationForLookup("  123 456 ")).isEqualTo("123456");
        assertThat(translator.normalizeRegistrationForLookup(" ")).isNull();
        assertThat(translator.normalizedToken("  Mestiça  ")).isEqualTo("MESTICA");
    }

    @Test
    void mapsRawSearchItemWithNormalizedFieldsAndWarnings() {
        var item = translator.normalizeSearchItem(GoatAbccRawSearchItemVO.builder()
                .externalId(" A-1 ")
                .nome("  ZENDA  ")
                .situacao("RGD")
                .sexo("Fêmea")
                .raca("SAANEN")
                .tod(" 12345 ")
                .toe(" 00001 ")
                .build());

        assertThat(item.getExternalSource()).isEqualTo("ABCC_PUBLIC");
        assertThat(item.getExternalId()).isEqualTo("A-1");
        assertThat(item.getNome()).isEqualTo("ZENDA");
        assertThat(item.getNormalizedGender()).isEqualTo(Gender.FEMEA);
        assertThat(item.getNormalizedBreed()).isEqualTo(GoatBreed.SAANEN);
        assertThat(item.getNormalizedStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(item.getNormalizationWarnings()).isEmpty();
    }

    @Test
    void mapsPreviewToGoatRequestAndPreservesRequiredFields() {
        GoatAbccPreviewResponseVO preview = translator.toPreview(rawPreview(
                "Macho", "SAANEN", "RGD", "PO", "10/01/2020"), 9L, "Capril");
        preview.setFatherRegistrationNumber(" PAI-1 ");
        preview.setMotherRegistrationNumber(" MAE-1 ");

        GoatRequestVO request = translator.buildGoatRequestFromPreview(preview);

        assertThat(request.getRegistrationNumber()).isEqualTo("1400810001");
        assertThat(request.getName()).isEqualTo("ANIMAL ABCC");
        assertThat(request.getBirthDate()).isEqualTo(LocalDate.of(2020, 1, 10));
        assertThat(request.getTod()).isEqualTo("12345");
        assertThat(request.getToe()).isEqualTo("00001");
        assertThat(request.getFatherRegistrationNumber()).isEqualTo("PAI-1");
        assertThat(request.getMotherRegistrationNumber()).isEqualTo("MAE-1");
    }

    @Test
    void rejectsMissingMandatoryImportFields() {
        GoatAbccPreviewResponseVO preview = translator.toPreview(rawPreview(
                "Macho", "SAANEN", "RGD", "PO", "10/01/2020"), 1L, "Fazenda");

        preview.setRegistrationNumber(null);
        assertThatThrownBy(() -> translator.buildGoatRequestFromPreview(preview))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Registro ABCC ausente");

        preview.setRegistrationNumber("1400810001");
        preview.setTod(null);
        assertThatThrownBy(() -> translator.buildGoatRequestFromPreview(preview))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("TOD ABCC ausente");
    }

    private GoatAbccRawPreviewVO rawPreview(
            String gender,
            String breed,
            String status,
            String category,
            String birthDate
    ) {
        return GoatAbccRawPreviewVO.builder()
                .externalId("A-001")
                .nome("ANIMAL ABCC")
                .registro("1400810001")
                .criador("CAPRIL BOCAINA")
                .raca(breed)
                .pelagem("BRANCA")
                .situacao(status)
                .sexo(gender)
                .categoria(category)
                .tod("12345")
                .toe("00001")
                .dataNascimento(birthDate)
                .paiNome("PAI")
                .paiRegistro("1400810002")
                .maeNome("MAE")
                .maeRegistro("1400810003")
                .build();
    }
}
