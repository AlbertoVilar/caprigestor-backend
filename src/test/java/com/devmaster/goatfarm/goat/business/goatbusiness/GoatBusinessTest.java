package com.devmaster.goatfarm.goat.business.goatbusiness;

// ===== IMPORTS DO PROJETO =====
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.genealogy.business.genealogyservice.GenealogyBusiness;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.model.entity.Goat;

// ===== IMPORTS DO JUNIT 5 =====
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

// ===== IMPORTS DO MOCKITO =====
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

// ===== IMPORTS DO ASSERTJ =====
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// ===== IMPORTS DO SPRING TEST =====
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// ===== IMPORTS DO JAVA =====
import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class GoatBusinessTest {

    // ===== MOCKS (Dependências do GoatBusiness) =====
    @Mock
    private GoatDAO goatDAO;

    @Mock
    private GoatFarmDAO goatFarmDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private GenealogyBusiness genealogyBusiness;

    @Mock
    private OwnershipService ownershipService;

    @Mock
    private GoatMapper goatMapper;

    @InjectMocks
    private GoatBusiness goatBusiness;

    // ===== DADOS DE TESTE =====
    private GoatRequestVO requestVO;
    private GoatResponseVO responseVO;
    private Goat goat;
    private GoatFarm goatFarm;
    private User currentUser;

    @BeforeEach
    void setUp() {
        // ===== MOCK DO CONTEXTO HTTP =====
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/goats");
        ServletRequestAttributes attributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attributes);

        // ===== USER =====
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setName("Alberto Vilar");
        currentUser.setEmail("alberto.vilar@test.com");

        // ===== GOAT FARM =====
        goatFarm = new GoatFarm();
        goatFarm.setId(1L);
        goatFarm.setName("Capril Vilar");
        goatFarm.setTod("16432");
        goatFarm.setUser(currentUser);

        // ===== GOAT REQUEST VO =====
        requestVO = new GoatRequestVO();
        requestVO.setRegistrationNumber("164322002");
        requestVO.setName("Xeque V Do Capril Vilar");
        requestVO.setGender(Gender.MACHO);
        requestVO.setBreed(GoatBreed.ALPINA);
        requestVO.setColor("Marrom");
        requestVO.setBirthDate(LocalDate.of(2025, 1, 1));
        requestVO.setStatus(GoatStatus.ATIVO);
        requestVO.setTod("16432");
        requestVO.setToe("22002");
        requestVO.setCategory(Category.PO);
        requestVO.setFatherRegistrationNumber(null);
        requestVO.setMotherRegistrationNumber(null);
        requestVO.setFarmId(1L);
        requestVO.setUserId(1L);

        // ===== GOAT (ENTIDADE) =====
        goat = new Goat();
        goat.setRegistrationNumber(requestVO.getRegistrationNumber());
        goat.setName(requestVO.getName());
        goat.setGender(requestVO.getGender());
        goat.setBreed(requestVO.getBreed());
        goat.setColor(requestVO.getColor());
        goat.setBirthDate(requestVO.getBirthDate());
        goat.setStatus(requestVO.getStatus());
        goat.setTod(requestVO.getTod());
        goat.setToe(requestVO.getToe());
        goat.setCategory(requestVO.getCategory());
        goat.setFarm(goatFarm);
        goat.setUser(currentUser);
        goat.setFather(null);
        goat.setMother(null);

        // ===== GOAT RESPONSE VO =====
        responseVO = new GoatResponseVO();
        responseVO.setRegistrationNumber(goat.getRegistrationNumber());
        responseVO.setName(goat.getName());
        responseVO.setGender(goat.getGender());
        responseVO.setBreed(goat.getBreed());
        responseVO.setColor(goat.getColor());
        responseVO.setBirthDate(goat.getBirthDate());
        responseVO.setStatus(goat.getStatus());
        responseVO.setTod(goat.getTod());
        responseVO.setToe(goat.getToe());
        responseVO.setCategory(goat.getCategory());
        responseVO.setFarmId(goatFarm.getId());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    // =====// ===== TESTES =====
    @Test
    @DisplayName("Deve criar Goat com sucesso")
    void shouldCreateGoatSuccessfully() {

        /** === Aqui vai a lógica do seu teste === **/

        // 1. Mock verifyFarmOwnership (método void - usa doNothing)
        doNothing().when(ownershipService).verifyFarmOwnership(1L);

        // 2. Mock existsById (retorna false para indicar que não há duplicidade)
        when(goatDAO.existsById("164322002")).thenReturn(false);

        // 3. Mock findFarmEntityById (busca a fazenda)
        when(goatFarmDAO.findFarmEntityById(1L)).thenReturn(goatFarm);

        // 4. Mock findByRegistrationNumber para pai e mãe (ambos null)
        when(goatDAO.findByRegistrationNumber(null)).thenReturn(Optional.empty());

        // 5. Mock toEntity (converte VO para entidade)
        when(goatMapper.toEntity(requestVO)).thenReturn(goat);

        // 6. Mock getCurrentUser (pega usuário logado)
        when(ownershipService.getCurrentUser()).thenReturn(currentUser);

        // 7. Mock save (salva no banco)
        when(goatDAO.save(any(Goat.class))).thenReturn(goat);

        // 8. Mock createGenealogy (CORRIGIDO: usa when/thenReturn para método que retorna valor)
        when(genealogyBusiness.createGenealogy(1L, "164322002")).thenReturn(null); // Retornamos null pois não nos importamos com o resultado aqui

        // 9. Mock toResponseVO (converte entidade para VO)
        when(goatMapper.toResponseVO(goat)).thenReturn(responseVO);

        // ===== ACT (Executar a ação) ===== //
        GoatResponseVO resultado = goatBusiness.createGoat(1L, requestVO);

        // ===== ASSERT (Verificar o resultado) ===== //
        assertThat(resultado).isNotNull();
        assertThat(resultado.getRegistrationNumber()).isEqualTo("164322002");
        assertThat(resultado.getName()).isEqualTo("Xeque V Do Capril Vilar");
        assertThat(resultado.getGender()).isEqualTo(Gender.MACHO);
        assertThat(resultado.getBreed()).isEqualTo(GoatBreed.ALPINA);
        assertThat(resultado.getColor()).isEqualTo("Marrom");
        assertThat(resultado.getBirthDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(resultado.getStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(resultado.getTod()).isEqualTo("16432");
        assertThat(resultado.getToe()).isEqualTo("22002");
        assertThat(resultado.getCategory()).isEqualTo(Category.PO);
        assertThat(resultado.getFarmId()).isEqualTo(1L);

        // ===== VERIFY (Confirmar que os métodos foram chamados) ===== //
        verify(ownershipService, times(1)).verifyFarmOwnership(1L);
        verify(goatDAO, times(1)).existsById("164322002");
        verify(goatFarmDAO, times(1)).findFarmEntityById(1L);
        verify(goatDAO, times(2)).findByRegistrationNumber(null); // Chamado para pai e mãe
        verify(goatMapper, times(1)).toEntity(requestVO);
        verify(ownershipService, times(1)).getCurrentUser();
        verify(goatDAO, times(1)).save(any(Goat.class));
        verify(genealogyBusiness, times(1)).createGenealogy(1L, "164322002");
        verify(goatMapper, times(1)).toResponseVO(goat);
    }
}
