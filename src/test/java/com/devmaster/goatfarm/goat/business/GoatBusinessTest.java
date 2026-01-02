package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
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
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoatBusinessTest {

    @Mock private GoatDAO goatDAO;
    @Mock private GoatFarmDAO goatFarmDAO;
    @Mock private UserDAO userDAO;
    @Mock private GenealogyBusiness genealogyBusiness;
    @Mock private OwnershipService ownershipService;
    @Mock private GoatMapper goatMapper;

    @InjectMocks private GoatBusiness goatBusiness;

    /**
     * Helper: cria um GoatRequestVO válido para cenários de fluxo feliz.
     *
     * <p>
     * Este método existe para evitar que testes cujo foco NÃO é validação de campos
     * (ex.: criação bem-sucedida, associação de pai/mãe, persistência correta)
     * falhem por regras básicas do domínio.
     * </p>
     *
     * <p>
     * Ele deve ser utilizado apenas em testes onde o objetivo é validar o
     * comportamento principal do caso de uso (happy path).
     * </p>
     *
     * <p>
     * Importante:
     * <ul>
     *   <li>Este helper NÃO deve ser usado em testes de erro (Duplicate, NotFound, Ownership).</li>
     *   <li>Campos incluídos aqui representam o mínimo necessário para um cenário válido.</li>
     *   <li>Novos campos só devem ser adicionados se uma regra de negócio realmente exigir.</li>
     * </ul>
     * </p>
     */
    private GoatRequestVO validGoatRequestVO() {
        GoatRequestVO vo = new GoatRequestVO();
        vo.setRegistrationNumber("164322002");
        vo.setName("Cabra Teste");
        vo.setGender(Gender.FEMEA);
        vo.setBreed(GoatBreed.ALPINA);
        vo.setBirthDate(LocalDate.of(2024, 1, 1));
        vo.setStatus(GoatStatus.ATIVO);
        vo.setCategory(Category.PO);
        return vo;
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Test
    @DisplayName("createGoat: deve salvar cabra com farm + user e setar pai/mãe quando informados")
    void createGoat_shouldSaveWithFarmUserAndParents_whenRequestHasParents() {
        // =========================
        // Arrange
        // =========================
        Long farmId = 1L;

        // Registro do filhote (TOE 16432 + TOD 26001 -> 2026, ordem 001)
        GoatRequestVO requestVO = validGoatRequestVO();
        requestVO.setRegistrationNumber("1643226001");

        // Registro do pai e mãe coerentes com domínio
        String fatherReg = "1643225001"; // TOE 16432 + TOD 25001
        String motherReg = "1643225002"; // TOE 16432 + TOD 25002
        requestVO.setFatherRegistrationNumber(fatherReg);
        requestVO.setMotherRegistrationNumber(motherReg);

        GoatFarm farm = new GoatFarm();
        farm.setId(farmId);

        User currentUser = new User();
        currentUser.setId(99L);

        Goat father = new Goat();
        father.setRegistrationNumber(fatherReg);

        Goat mother = new Goat();
        mother.setRegistrationNumber(motherReg);

        // Entidade que o mapper cria a partir do VO
        Goat mappedGoat = new Goat();

        // Entidade salva (pode ser a mesma instância ou outra; aqui deixo a mesma para simplificar)
        Goat savedGoat = mappedGoat;
        savedGoat.setRegistrationNumber("1643226001");

        GoatResponseVO responseVO = new GoatResponseVO();

        // ownership ok
        doNothing().when(ownershipService).verifyFarmOwnership(farmId);

        // checa duplicidade (registro não existe)
        when(goatDAO.existsById("1643226001")).thenReturn(false);

        // busca farm e pais
        when(goatFarmDAO.findFarmEntityById(farmId)).thenReturn(farm);
        when(goatDAO.findByRegistrationNumber(fatherReg)).thenReturn(Optional.of(father));
        when(goatDAO.findByRegistrationNumber(motherReg)).thenReturn(Optional.of(mother));

        // mapper e user atual
        when(goatMapper.toEntity(requestVO)).thenReturn(mappedGoat);
        when(ownershipService.getCurrentUser()).thenReturn(currentUser);

        // save + response
        when(goatDAO.save(any(Goat.class))).thenReturn(savedGoat);
        when(goatMapper.toResponseVO(savedGoat)).thenReturn(responseVO);

        // =========================
        // Act
        // =========================
        GoatResponseVO result = goatBusiness.createGoat(farmId, requestVO);

        // =========================
        // Assert (resultado)
        // =========================
        assertThat(result).isSameAs(responseVO);

        // =========================
        // Verify (capturando o objeto salvo)
        // =========================
        ArgumentCaptor<Goat> goatCaptor = ArgumentCaptor.forClass(Goat.class);
        verify(goatDAO).save(goatCaptor.capture());

        Goat goatSaved = goatCaptor.getValue();

        // Asserções de estado (isso é o coração do teste!)
        assertThat(goatSaved.getFarm()).isSameAs(farm);
        assertThat(goatSaved.getUser()).isSameAs(currentUser);
        assertThat(goatSaved.getFather()).isSameAs(father);
        assertThat(goatSaved.getMother()).isSameAs(mother);

        // =========================
        // Verify (interações principais)
        // =========================
        verify(ownershipService, times(1)).verifyFarmOwnership(farmId);
        verify(goatDAO, times(1)).existsById("1643226001");
        verify(goatFarmDAO, times(1)).findFarmEntityById(farmId);

        verify(goatDAO, times(1)).findByRegistrationNumber(fatherReg);
        verify(goatDAO, times(1)).findByRegistrationNumber(motherReg);

        verify(goatMapper, times(1)).toEntity(requestVO);
        verify(ownershipService, times(1)).getCurrentUser();

        // Como o registro salvo NÃO é nulo, deve criar genealogia
        verify(genealogyBusiness, times(1)).createGenealogy(farmId, "1643226001");

        verify(goatMapper, times(1)).toResponseVO(savedGoat);
    }


    @Test
    @DisplayName("createGoat: deve lançar DuplicateEntityException quando registrationNumber já existe")
    void createGoat_shouldThrowDuplicateEntityException_whenRegistrationNumberAlreadyExists() {
        // =========================
        // Arrange
        // =========================
        Long farmId = 1L;

        GoatRequestVO requestVO = new GoatRequestVO();
        requestVO.setRegistrationNumber("164322002"); // único campo necessário para cair no if de duplicidade

        // ownership passa (não explode)
        doNothing().when(ownershipService).verifyFarmOwnership(farmId);

        // simula duplicidade
        when(goatDAO.existsById("164322002")).thenReturn(true);

        // =========================
        // Act + Assert
        // =========================
        assertThrows(DuplicateEntityException.class,
                () -> goatBusiness.createGoat(farmId, requestVO));

        // =========================
        // Verify (garante fail-fast)
        // =========================
        verify(ownershipService, times(1)).verifyFarmOwnership(farmId);
        verify(goatDAO, times(1)).existsById("164322002");

        // Como caiu na duplicidade, nada abaixo deve acontecer:
        verifyNoInteractions(goatFarmDAO);
        verifyNoInteractions(goatMapper);
        verify(goatDAO, never()).save(any());
        verify(genealogyBusiness, never()).createGenealogy(anyLong(), anyString());
    }


    @Test
    @DisplayName("createGoat: NÃO deve chamar genealogyBusiness quando registrationNumber do goat salvo for nulo")
    void createGoat_shouldNotCallGenealogy_whenSavedGoatRegistrationNumberIsNull() {
        // =========================
        // Arrange
        // =========================
        Long farmId = 1L;

        // Podemos usar o helper porque é fluxo feliz (não é teste de erro)
        GoatRequestVO requestVO = validGoatRequestVO();

        GoatFarm farm = new GoatFarm();
        User currentUser = new User();

        Goat mappedGoat = new Goat(); // entidade produzida pelo mapper a partir do VO

        // O ponto do teste: o goat salvo volta com registrationNumber = null
        Goat savedGoat = new Goat();
        savedGoat.setRegistrationNumber(null);

        GoatResponseVO responseVO = new GoatResponseVO();

        doNothing().when(ownershipService).verifyFarmOwnership(farmId);

        // Como o request tem registrationNumber != null, o business checa duplicidade.
        when(goatDAO.existsById(requestVO.getRegistrationNumber())).thenReturn(false);

        when(goatFarmDAO.findFarmEntityById(farmId)).thenReturn(farm);
        when(goatMapper.toEntity(requestVO)).thenReturn(mappedGoat);

        when(ownershipService.getCurrentUser()).thenReturn(currentUser);

        // save devolve o objeto com registrationNumber nulo (branch que queremos)
        when(goatDAO.save(any(Goat.class))).thenReturn(savedGoat);

        when(goatMapper.toResponseVO(savedGoat)).thenReturn(responseVO);

        // =========================
        // Act
        // =========================
        GoatResponseVO result = goatBusiness.createGoat(farmId, requestVO);

        // =========================
        // Assert
        // =========================
        assertThat(result).isSameAs(responseVO);

        // =========================
        // Verify
        // =========================
        verify(ownershipService, times(1)).verifyFarmOwnership(farmId);
        verify(goatDAO, times(1)).existsById(requestVO.getRegistrationNumber());
        verify(goatFarmDAO, times(1)).findFarmEntityById(farmId);
        verify(goatMapper, times(1)).toEntity(requestVO);
        verify(ownershipService, times(1)).getCurrentUser();
        verify(goatDAO, times(1)).save(any(Goat.class));
        verify(goatMapper, times(1)).toResponseVO(savedGoat);

        // O ponto do teste: NÃO deve criar genealogia quando o registro salvo é nulo
        verify(genealogyBusiness, never()).createGenealogy(anyLong(), anyString());

        // Pais não foram informados no helper -> findByRegistrationNumber não deve ser chamado
        verify(goatDAO, never()).findByRegistrationNumber(any());
    }


    @Test
    @DisplayName("updateGoat: deve lançar ResourceNotFoundException quando cabra não existe na fazenda")
    void updateGoat_shouldThrowResourceNotFoundException_whenGoatNotFoundInFarm() {
        // =========================
        // Arrange
        // =========================
        Long farmId = 1L;
        String goatId = "164322002";

        // Para este teste, o conteúdo do VO é irrelevante,
        // porque a execução deve falhar ANTES de usar os campos dele.
        GoatRequestVO requestVO = new GoatRequestVO();

        // ownership passa
        doNothing().when(ownershipService).verifyFarmOwnership(farmId);

        // goat não existe nessa fazenda
        when(goatDAO.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.empty());

        // =========================
        // Act + Assert
        // =========================
        assertThrows(ResourceNotFoundException.class,
                () -> goatBusiness.updateGoat(farmId, goatId, requestVO));

        // =========================
        // Verify (fail-fast)
        // =========================
        verify(ownershipService, times(1)).verifyFarmOwnership(farmId);
        verify(goatDAO, times(1)).findByIdAndFarmId(goatId, farmId);

        // Como não achou, não pode continuar o fluxo:
        verify(goatDAO, never()).findByRegistrationNumber(any());
        verify(goatMapper, never()).updateEntity(any(), any(), any(), any());
        verify(goatDAO, never()).save(any());
        verify(goatMapper, never()).toResponseVO(any());
    }


    @Test
    @DisplayName("updateGoat: deve chamar mapper.updateEntity passando pai/mãe resolvidos e salvar")
    void updateGoat_shouldUpdateEntityWithResolvedParents_andSave() {
        // =========================
        // Arrange
        // =========================
        Long farmId = 1L;
        String goatId = "1643226001"; // exemplo: TOE 16432 + TOD 26001 (ano 26, ordem 001)

        GoatRequestVO requestVO = new GoatRequestVO();

        // Números de registro coerentes com o domínio: TOE + TOD
        // TOE (fazenda): 16432
        // TOD (ano+ordem): 25001 e 25002 (ano 25, ordem 001/002)
        String fatherReg = "1643225001";
        String motherReg = "1643225002";

        requestVO.setFatherRegistrationNumber(fatherReg);
        requestVO.setMotherRegistrationNumber(motherReg);

        Goat goatToUpdate = new Goat(); // entidade atual encontrada no banco
        Goat father = new Goat();
        Goat mother = new Goat();

        Goat savedGoat = goatToUpdate; // normalmente o mesmo objeto é persistido/retornado
        GoatResponseVO responseVO = new GoatResponseVO();

        // ownership ok
        doNothing().when(ownershipService).verifyFarmOwnership(farmId);

        // cabra existe nessa fazenda
        when(goatDAO.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.of(goatToUpdate));

        // resolve pai/mãe
        when(goatDAO.findByRegistrationNumber(fatherReg)).thenReturn(Optional.of(father));
        when(goatDAO.findByRegistrationNumber(motherReg)).thenReturn(Optional.of(mother));

        // save
        when(goatDAO.save(goatToUpdate)).thenReturn(savedGoat);

        // response
        when(goatMapper.toResponseVO(savedGoat)).thenReturn(responseVO);

        // =========================
        // Act
        // =========================
        GoatResponseVO result = goatBusiness.updateGoat(farmId, goatId, requestVO);

        // =========================
        // Assert
        // =========================
        assertThat(result).isSameAs(responseVO);

        // =========================
        // Verify
        // =========================
        verify(ownershipService, times(1)).verifyFarmOwnership(farmId);
        verify(goatDAO, times(1)).findByIdAndFarmId(goatId, farmId);

        // buscou pai e mãe (porque foram informados no request)
        verify(goatDAO, times(1)).findByRegistrationNumber(fatherReg);
        verify(goatDAO, times(1)).findByRegistrationNumber(motherReg);

        // ponto principal: mapper recebeu pai/mãe resolvidos corretamente
        verify(goatMapper, times(1)).updateEntity(goatToUpdate, requestVO, father, mother);

        verify(goatDAO, times(1)).save(goatToUpdate);
        verify(goatMapper, times(1)).toResponseVO(savedGoat);

        // updateGoat não deve chamar genealogia
        verifyNoInteractions(genealogyBusiness);
    }

    @Test
    @DisplayName("deleteGoat: deve lançar ResourceNotFoundException quando cabra não existe na fazenda")
    void deleteGoat_shouldThrowResourceNotFoundException_whenGoatNotFoundInFarm() {
        // =========================
        // Arrange
        // =========================
        Long farmId = 1L;
        String goatId = "1643226001";

        // ownership passa
        doNothing().when(ownershipService).verifyGoatOwnership(farmId, goatId);

        // cabra não existe nessa fazenda
        when(goatDAO.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.empty());

        // =========================
        // Act + Assert
        // =========================
        assertThrows(ResourceNotFoundException.class,
                () -> goatBusiness.deleteGoat(farmId, goatId));

        // =========================
        // Verify (fail-fast)
        // =========================
        verify(ownershipService, times(1)).verifyGoatOwnership(farmId, goatId);
        verify(goatDAO, times(1)).findByIdAndFarmId(goatId, farmId);

        // não deve deletar nada
        verify(goatDAO, never()).delete(any());
    }


    @Test
    @DisplayName("deleteGoat: deve deletar com sucesso quando ownership ok e cabra existe")
    void deleteGoat_shouldDeleteSuccessfully_whenOwnershipOkAndGoatExists() {
        // =========================
        // Arrange
        // =========================
        Long farmId = 1L;
        String goatId = "1643226001";

        Goat goat = new Goat();
        goat.setRegistrationNumber(goatId);

        // ownership passa
        doNothing().when(ownershipService).verifyGoatOwnership(farmId, goatId);

        // cabra existe nessa fazenda
        when(goatDAO.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.of(goat));

        // =========================
        // Act
        // =========================
        goatBusiness.deleteGoat(farmId, goatId);

        // =========================
        // Verify
        // =========================
        verify(ownershipService, times(1)).verifyGoatOwnership(farmId, goatId);
        verify(goatDAO, times(1)).findByIdAndFarmId(goatId, farmId);
        verify(goatDAO, times(1)).delete(goat);
    }

}
