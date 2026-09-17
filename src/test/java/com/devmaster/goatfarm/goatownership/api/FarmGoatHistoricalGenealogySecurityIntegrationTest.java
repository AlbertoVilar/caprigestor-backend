package com.devmaster.goatfarm.goatownership.api;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.out.FarmAccessQueryPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.FarmOwnerQueryPort;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyNodeSource;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeNode;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.controller.FarmGoatRegistryController;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalGenealogyQueryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@WebMvcTest(FarmGoatRegistryController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({OwnershipService.class, GlobalExceptionHandler.class, FarmGoatHistoricalGenealogySecurityIntegrationTest.MethodSecurityTestConfiguration.class})
@ActiveProfiles("test")
class FarmGoatHistoricalGenealogySecurityIntegrationTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FarmGoatRegistryController controller;

    @Autowired
    private OwnershipService ownershipService;

    @MockBean
    private CurrentPrincipalQueryUseCase currentPrincipalQuery;

    @MockBean
    private FarmAccessQueryPort farmAccessQueryPort;

    @MockBean
    private FarmOwnerQueryPort farmOwnerQueryPort;

    @MockBean
    private com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort goatFarmPersistencePort;

    @MockBean
    private com.devmaster.goatfarm.farm.application.ports.in.FarmExistenceQueryUseCase farmExistenceQueryUseCase;

    @MockBean
    private com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase farmGoatRegistryQueryUseCase;

    @MockBean
    private com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatRegistryApiMapper farmGoatRegistryApiMapper;

    @MockBean
    private com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalGenealogyApiMapper historicalGenealogyMapper;

    @MockBean
    private FarmGoatHistoricalGenealogyQueryUseCase historicalGenealogyQueryUseCase;

    @BeforeEach
    void setUp() {
        when(farmOwnerQueryPort.findOwnerId(10L)).thenReturn(Optional.of(100L));
        when(farmAccessQueryPort.existsOperatorLink(10L, 200L)).thenReturn(true);
        when(farmAccessQueryPort.existsOperatorLink(10L, 300L)).thenReturn(false);

        when(currentPrincipalQuery.requireCurrent()).thenAnswer(invocation -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                throw new UnauthorizedException("Unauthenticated");
            }
            Long userId = 100L;
            String username = auth.getName();
            if ("owner100".equals(username)) userId = 100L;
            else if ("owner999".equals(username)) userId = 999L;
            else if ("operator200".equals(username)) userId = 200L;
            else if ("operator300".equals(username)) userId = 300L;
            else if ("viewer400".equals(username)) userId = 400L;

            Set<String> authorities = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            return new AuthenticatedPrincipal(userId, username, username + "@test.com", authorities);
        });

        GoatId goatId = GoatId.of(42L);
        GenealogyTreeNode principal = new GenealogyTreeNode("animalPrincipal", "Estrela", "RG42", GenealogyNodeSource.LOCAL, goatId);
        GenealogyTreeSnapshot tree = new GenealogyTreeSnapshot(principal, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyNodeDTO principalDto =
                new com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyNodeDTO("animalPrincipal", "Estrela", "RG42", GenealogyNodeSource.LOCAL, 42L);
        com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyResponseDTO responseDto =
                new com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyResponseDTO(principalDto, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(historicalGenealogyQueryUseCase.findHistoricalGenealogy(10L, goatId, false)).thenReturn(Optional.of(tree));
        when(historicalGenealogyMapper.toResponse(tree)).thenReturn(responseDto);
    }

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
        assertThat(ownershipService).isNotNull();
    }

    @Test
    @WithMockUser(username = "adminUser", roles = "ADMIN")
    void admin_withRegistryMembership_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/10/goat-registry/technical-42/genealogy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalPrincipal.name").value("Estrela"));
    }

    @Test
    @WithMockUser(username = "owner100", roles = "FARM_OWNER")
    void officialFarmOwner_matchingFarmOwner_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/10/goat-registry/technical-42/genealogy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalPrincipal.name").value("Estrela"));
    }

    @Test
    @WithMockUser(username = "owner999", roles = "FARM_OWNER")
    void farmOwner_nonMatchingFarmOwner_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/10/goat-registry/technical-42/genealogy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operator200", roles = "OPERATOR")
    void linkedOperator_withRegistryMembership_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/10/goat-registry/technical-42/genealogy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalPrincipal.name").value("Estrela"));
    }

    @Test
    @WithMockUser(username = "operator300", roles = "OPERATOR")
    void unlinkedOperator_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/10/goat-registry/technical-42/genealogy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "viewer400", roles = "VIEWER")
    void authenticatedPrincipalWithoutManagePermission_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/10/goat-registry/technical-42/genealogy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/10/goat-registry/technical-42/genealogy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
