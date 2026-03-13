package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatAbccImportBusinessTest {

    @Mock
    private OwnershipService ownershipService;
    @Mock
    private GoatFarmPersistencePort goatFarmPort;
    @Mock
    private GoatAbccPublicQueryPort abccPublicQueryPort;
    @Mock
    private GoatManagementUseCase goatManagementUseCase;
    @Mock
    private EntityFinder entityFinder;

    private GoatAbccImportBusiness business;

    @BeforeEach
    void setUp() {
        business = new GoatAbccImportBusiness(
                ownershipService,
                goatFarmPort,
                abccPublicQueryPort,
                goatManagementUseCase,
                entityFinder
        );

        lenient().when(entityFinder.findOrThrow(any(), any())).thenAnswer(invocation -> {
            java.util.function.Supplier<Optional<?>> supplier = invocation.getArgument(0);
            String errorMsg = invocation.getArgument(1);
            return supplier.get().orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException(errorMsg));
        });
    }

    @Test
    void shouldSearchAndNormalizeAbccResult() {
        doNothing().when(ownershipService).verifyFarmOwnership(1L);
        when(abccPublicQueryPort.search(any())).thenReturn(GoatAbccRawSearchResultVO.builder()
                .currentPage(1)
                .totalPages(10)
                .offset(7)
                .currentUrl("/x.php?m=siscapri.Genealogia")
                .items(List.of(
                        GoatAbccRawSearchItemVO.builder()
                                .externalId("4044")
                                .nome("FALCÃO DO CAPRIL DA PRATA")
                                .situacao("RGD")
                                .dna("Sim")
                                .tod("14332")
                                .toe("14017")
                                .criador("CRIADOR TESTE")
                                .afixo("CRS")
                                .dataNascimento("16/05/2014")
                                .sexo("Macho")
                                .raca("SAANEN")
                                .pelagem("BRANCA")
                                .build()
                ))
                .build());

        var response = business.search(1L, GoatAbccSearchRequestVO.builder()
                .raceId(9)
                .affix("CRS")
                .page(1)
                .build());

        assertThat(response).isNotNull();
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getNormalizedGender()).isEqualTo(Gender.MACHO);
        assertThat(response.getItems().getFirst().getNormalizedBreed()).isEqualTo(GoatBreed.SAANEN);
        assertThat(response.getItems().getFirst().getNormalizedStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(response.getItems().getFirst().getNormalizationWarnings()).isEmpty();
        verify(ownershipService).verifyFarmOwnership(1L);
    }

    @Test
    void shouldPreviewAndNormalizeWithWarningsWhenNeeded() {
        GoatFarm farm = new GoatFarm();
        farm.setId(1L);
        farm.setName("Capril Vilar");
        User user = new User();
        user.setName("Alberto Vilar");

        doNothing().when(ownershipService).verifyFarmOwnership(1L);
        when(goatFarmPort.findById(1L)).thenReturn(Optional.of(farm));
        when(ownershipService.getCurrentUser()).thenReturn(user);
        when(abccPublicQueryPort.preview("4044")).thenReturn(GoatAbccRawPreviewVO.builder()
                .externalId("4044")
                .nome("FALCÃO DO CAPRIL DA PRATA")
                .registro("1433214017")
                .criador("CRIADOR")
                .proprietario("PROPRIETÁRIO")
                .raca("ANGORÁ")
                .pelagem("BRANCA")
                .situacao("RGD")
                .sexo("Macho")
                .categoria("PCOD")
                .tod("14332")
                .toe("14017")
                .dataNascimento("31/02/2014")
                .paiNome("DOM")
                .paiRegistro("1433212006")
                .maeNome("RAIANE")
                .maeRegistro("1427908023")
                .build());

        var response = business.preview(1L, GoatAbccPreviewRequestVO.builder().externalId("4044").build());

        assertThat(response).isNotNull();
        assertThat(response.getExternalSource()).isEqualTo("ABCC_PUBLIC");
        assertThat(response.getRegistrationNumber()).isEqualTo("1433214017");
        assertThat(response.getGender()).isEqualTo(Gender.MACHO);
        assertThat(response.getBreed()).isNull();
        assertThat(response.getStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(response.getCategory()).isEqualTo(Category.PC);
        assertThat(response.getBirthDate()).isNull();
        assertThat(response.getUserName()).isEqualTo("Alberto Vilar");
        assertThat(response.getFarmName()).isEqualTo("Capril Vilar");
        assertThat(response.getNormalizationWarnings()).isNotEmpty();
    }

    @Test
    void shouldConfirmByReusingGoatCreateFlow() {
        GoatRequestVO requestVO = new GoatRequestVO();
        requestVO.setRegistrationNumber("1643218012");
        requestVO.setName("XEQUE V DO CAPRIL VILAR");
        requestVO.setGender(Gender.MACHO);
        requestVO.setBreed(GoatBreed.ALPINA);
        requestVO.setColor("CHAMOISÉE");
        requestVO.setBirthDate(LocalDate.of(2018, 6, 27));
        requestVO.setStatus(GoatStatus.ATIVO);

        GoatResponseVO expected = new GoatResponseVO();
        expected.setRegistrationNumber("1643218012");

        when(goatManagementUseCase.createGoat(1L, requestVO)).thenReturn(expected);

        GoatResponseVO response = business.confirm(1L, "4044", requestVO);

        assertThat(response).isSameAs(expected);
        verify(goatManagementUseCase).createGoat(1L, requestVO);
    }

    @Test
    void shouldReturnBusinessRuleWhenAbccSearchFails() {
        doNothing().when(ownershipService).verifyFarmOwnership(1L);
        when(abccPublicQueryPort.search(any())).thenThrow(new RuntimeException("ABCC indisponível"));

        assertThatThrownBy(() -> business.search(1L, GoatAbccSearchRequestVO.builder()
                        .raceId(9)
                        .affix("CRS")
                        .page(1)
                        .build()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Não foi possível consultar a ABCC pública");
    }

    @Test
    void shouldReturnBusinessRuleWhenConfirmPayloadIsMissing() {
        assertThatThrownBy(() -> business.confirm(1L, "4044", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Dados do animal são obrigatórios");
    }
}

