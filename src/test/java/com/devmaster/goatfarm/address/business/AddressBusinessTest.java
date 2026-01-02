package com.devmaster.goatfarm.address.business;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AddressBusinessTest (Unit Test com Mockito)
 *
 * OBJETIVO:
 * - Testar APENAS a lógica do AddressBusiness (camada de negócio)
 * - Sem Spring Context, sem banco real, sem HTTP
 * - Tudo que é dependência externa vira Mock (DAO, Mapper, Security)
 *
 * POR QUE ISSO É BOM?
 * - Teste muito rápido
 * - Fácil de entender
 * - Isola regra de negócio (arquitetura limpa)
 *
 * PADRÃO AAA (Arrange / Act / Assert):
 * - Arrange: preparar o cenário (VOs, entidades, e respostas dos mocks)
 * - Act: executar o método que está sendo testado
 * - Assert: verificar o resultado e/ou exceção
 * - Verify (Mockito): confirmar interações (quem foi chamado, quantas vezes, e com quais dados)
 */
@ExtendWith(MockitoExtension.class)
public class AddressBusinessTest {

    /**
     * @InjectMocks:
     * - Cria instância REAL do AddressBusiness
     * - Injeta automaticamente os mocks (@Mock) nas dependências do Business
     */
    @InjectMocks
    private AddressBusiness addressBusiness;

    /** Mock do DAO: simula banco/persistência */
    @Mock
    private AddressDAO addressDAO;

    /** Mock do Mapper: simula conversões VO <-> Entity */
    @Mock
    private AddressMapper addressMapper;

    /** Mock de segurança: simula verificação de ownership (autorização) */
    @Mock
    private OwnershipService ownershipService;

    /**
     * Helper: cria um AddressRequestVO válido (passa nas validações).
     *
     * Por que isso existe?
     * - Para que a maioria dos testes foque na regra principal (ex.: "cria", "busca", "não duplica"),
     *   sem falhar por validações que não são o foco do teste.
     */
    private AddressRequestVO validRequestVO() {
        AddressRequestVO vo = new AddressRequestVO();
        vo.setStreet("Rua das Flores");
        vo.setNeighborhood("Centro");
        vo.setCity("Rio de Janeiro");
        vo.setState("RJ");         // UF válida
        vo.setZipCode("12345678"); // CEP válido (8 dígitos)
        vo.setCountry("Brasil");   // regra atual do domínio: só Brasil
        return vo;
    }

    // ============================================================
    // findOrCreateAddressEntity(...) - regra de deduplicação
    // ============================================================

    /**
     * CENÁRIO 1 (Fail fast / validação):
     * - Se a UF for inválida, o Business deve lançar ValidationException
     * - E deve parar ali: não pode tocar em DAO nem Mapper
     *
     * O que isso prova?
     * - A validação do domínio funciona
     * - A validação acontece ANTES de chamar infraestrutura
     */
    @Test
    void shouldThrowValidationExceptionWhenStateIsInvalid() {
        // Arrange
        AddressRequestVO vo = validRequestVO();
        vo.setState("FF"); // UF inválida

        // Act + Assert (espera exception)
        assertThrows(ValidationException.class,
                () -> addressBusiness.findOrCreateAddressEntity(vo));

        // Verify (arquitetura): não deve tocar DAO/Mapper se validação falhou
        verifyNoInteractions(addressDAO);
        verifyNoInteractions(addressMapper);
    }

    /**
     * CENÁRIO 2 (Não duplicar):
     * - Se o DAO encontrar um Address exatamente igual, o Business deve:
     *   1) retornar o existente
     *   2) não mapear VO -> Entity
     *   3) não chamar createAddress
     *
     * Por que esse teste é valioso?
     * - Evita duplicações no banco
     * - Garante que a regra de "reutilizar o endereço" funciona
     */
    @Test
    void shouldReturnExistingAddressWhenExactMatchExists() {
        // Arrange
        AddressRequestVO vo = validRequestVO();
        Address existing = new Address();

        // Stub: DAO encontra o endereço (Optional.of)
        when(addressDAO.searchExactAddress(
                eq(vo.getStreet()),
                eq(vo.getNeighborhood()),
                eq(vo.getCity()),
                eq(vo.getState()),
                eq(vo.getZipCode())
        )).thenReturn(Optional.of(existing));

        // Act
        Address result = addressBusiness.findOrCreateAddressEntity(vo);

        // Assert: deve retornar o mesmo objeto encontrado
        assertSame(existing, result);

        // Verify: se já existe, não cria nem mapeia
        verify(addressDAO, never()).createAddress(any());
        verify(addressMapper, never()).toEntity(any(AddressRequestVO.class));
    }

    /**
     * CENÁRIO 3 (Criar quando não existe):
     * - Se o DAO NÃO encontrar um Address igual, o Business deve:
     *   1) mapear VO -> Entity
     *   2) persistir no DAO (createAddress)
     *   3) retornar o Address persistido (retorno do DAO)
     *
     * Esse teste prova o fluxo completo do "create se não existir".
     */
    @Test
    void shouldCreateNewAddressWhenExactMatchDoesNotExist() {
        // Arrange
        AddressRequestVO vo = validRequestVO();
        Address mapped = new Address(); // resultado do mapper
        Address saved = new Address();  // resultado do DAO após persistir

        // Stub 1: "não existe"
        when(addressDAO.searchExactAddress(
                eq(vo.getStreet()),
                eq(vo.getNeighborhood()),
                eq(vo.getCity()),
                eq(vo.getState()),
                eq(vo.getZipCode())
        )).thenReturn(Optional.empty());

        // Stub 2: mapper cria a entidade a partir do VO
        when(addressMapper.toEntity(vo)).thenReturn(mapped);

        // Stub 3: DAO persiste e devolve a entidade "salva"
        when(addressDAO.createAddress(mapped)).thenReturn(saved);

        // Act
        Address result = addressBusiness.findOrCreateAddressEntity(vo);

        // Assert: retorna exatamente o que o DAO devolveu como "salvo"
        assertSame(saved, result);

        // Verify: confirma que o fluxo aconteceu (mapear + salvar)
        verify(addressMapper).toEntity(vo);
        verify(addressDAO).createAddress(mapped);
    }

    // ============================================================
    // createAddress(farmId, requestVO) - criação com ownership + mapper + dao
    // ============================================================

    /**
     * CENÁRIO 4 (Create feliz):
     * - ownershipService.verifyFarmOwnership(farmId) deve ser chamado
     * - VO válido -> mapper.toEntity(vo)
     * - DAO.createAddress(entity)
     * - mapper.toResponseVO(saved)
     * - retorna AddressResponseVO
     *
     * O que esse teste prova?
     * - O método está orquestrando corretamente a criação
     * - Segurança (ownership) é verificada antes de persistir
     */
    @Test
    void shouldCreateAddressWhenDataIsValid() {
        // Arrange
        Long farmId = 1L;
        AddressRequestVO vo = validRequestVO();

        Address entity = new Address();
        Address saved = new Address();
        AddressResponseVO response = new AddressResponseVO();

        when(addressMapper.toEntity(vo)).thenReturn(entity);
        when(addressDAO.createAddress(entity)).thenReturn(saved);
        when(addressMapper.toResponseVO(saved)).thenReturn(response);

        // Act
        AddressResponseVO result = addressBusiness.createAddress(farmId, vo);

        // Assert
        assertSame(response, result);

        // Verify (ordem não é checada aqui, apenas presença das chamadas)
        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressMapper).toEntity(vo);
        verify(addressDAO).createAddress(entity);
        verify(addressMapper).toResponseVO(saved);
    }

    /**
     * CENÁRIO 5 (Create com dados inválidos):
     * - ownershipService.verifyFarmOwnership(farmId) acontece (está antes da validação no código atual)
     * - Validação falha -> lança ValidationException
     * - Não deve tocar persistência (DAO) nem gerar resposta (toResponseVO)
     *
     * O que esse teste prova?
     * - Falha por validação impede escrita no banco
     * - Mantém consistência (fail fast)
     */
    @Test
    void shouldThrowValidationExceptionOnCreateAddressWhenStateIsInvalid() {
        // Arrange
        Long farmId = 1L;
        AddressRequestVO vo = validRequestVO();
        vo.setState("FF");

        // Act + Assert
        assertThrows(ValidationException.class,
                () -> addressBusiness.createAddress(farmId, vo));

        // Verify: ownership é chamado antes da validação no método createAddress atual
        verify(ownershipService).verifyFarmOwnership(farmId);

        // Verify: não deve tocar infraestrutura de persistência nem produzir response
        verifyNoInteractions(addressDAO);
        verify(addressMapper, never()).toEntity(any(AddressRequestVO.class));
        verify(addressMapper, never()).toResponseVO(any(Address.class));
    }

    // ============================================================
    // findAddressById(farmId, addressId) - busca + not found
    // ============================================================

    /**
     * CENÁRIO 6 (Find por ID - sucesso):
     * - ownershipService.verifyFarmOwnership(farmId) deve ser chamado
     * - DAO.findByIdAndFarmId(addressId, farmId) retorna entity
     * - mapper.toResponseVO(entity) retorna response
     * - Business retorna response
     */
    @Test
    void shouldFindAddressByIdWhenExists() {
        // Arrange
        Long farmId = 1L;
        Long addressId = 10L;

        Address entity = new Address();
        AddressResponseVO response = new AddressResponseVO();

        when(addressDAO.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.of(entity));
        when(addressMapper.toResponseVO(entity)).thenReturn(response);

        // Act
        AddressResponseVO result = addressBusiness.findAddressById(farmId, addressId);

        // Assert
        assertSame(response, result);

        // Verify
        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressDAO).findByIdAndFarmId(addressId, farmId);
        verify(addressMapper).toResponseVO(entity);
    }

    /**
     * CENÁRIO 7 (Find por ID - not found):
     * - ownership é verificado
     * - DAO retorna Optional.empty()
     * - Business lança ResourceNotFoundException
     * - mapper não é chamado
     */
    @Test
    void shouldThrowResourceNotFoundWhenFindAddressByIdDoesNotExist() {
        // Arrange
        Long farmId = 1L;
        Long addressId = 10L;

        when(addressDAO.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> addressBusiness.findAddressById(farmId, addressId));

        // Verify
        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressDAO).findByIdAndFarmId(addressId, farmId);
        verifyNoInteractions(addressMapper);
    }
    /**
     * CENÁRIO 8 (Update feliz):
     * - ownershipService.verifyFarmOwnership(farmId) é chamado
     * - validação passa (VO válido)
     * - DAO.findByIdAndFarmId(...) encontra o Address atual
     * - mapper aplica update in-place: toEntity(current, requestVO)
     * - DAO.updateAddress(addressId, current) retorna entidade atualizada
     * - mapper.toResponseVO(updated) gera resposta
     *
     * O que esse teste prova?
     * - Fluxo completo de atualização está correto (busca -> atualiza -> salva -> responde)
     */
    @Test
    void shouldUpdateAddressWhenExistsAndDataIsValid() {
        // Arrange
        Long farmId = 1L;
        Long addressId = 10L;
        AddressRequestVO vo = validRequestVO();

        Address current = new Address();
        Address updated = new Address();
        AddressResponseVO response = new AddressResponseVO();

        when(addressDAO.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.of(current));
        when(addressDAO.updateAddress(addressId, current)).thenReturn(updated);
        when(addressMapper.toResponseVO(updated)).thenReturn(response);

        // Act
        AddressResponseVO result = addressBusiness.updateAddress(farmId, addressId, vo);

        // Assert
        assertSame(response, result);

        // Verify
        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressDAO).findByIdAndFarmId(addressId, farmId);

        // mapper atualiza o objeto existente (in-place)
        verify(addressMapper).toEntity(current, vo);

        // persistência e resposta
        verify(addressDAO).updateAddress(addressId, current);
        verify(addressMapper).toResponseVO(updated);
    }

    /**
     * CENÁRIO 9 (Update - not found):
     * - ownership é verificado
     * - DAO.findByIdAndFarmId(...) retorna empty
     * - Business lança ResourceNotFoundException
     * - mapper não é chamado
     * - DAO.updateAddress não é chamado
     *
     * O que esse teste prova?
     * - Não atualiza entidade inexistente
     * - Mantém regra de consistência e mensagens de erro corretas
     */
    @Test
    void shouldThrowResourceNotFoundWhenUpdateAddressDoesNotExist() {
        // Arrange
        Long farmId = 1L;
        Long addressId = 10L;
        AddressRequestVO vo = validRequestVO();

        when(addressDAO.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> addressBusiness.updateAddress(farmId, addressId, vo));

        // Verify
        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressDAO).findByIdAndFarmId(addressId, farmId);

        verifyNoInteractions(addressMapper);
        verify(addressDAO, never()).updateAddress(any(), any());
    }

    /**
     * CENÁRIO 10 (Delete feliz):
     * - ownership é verificado
     * - DAO.findByIdAndFarmId(...) confirma que o endereço pertence à fazenda
     * - DAO.deleteAddress(addressId) é chamado
     * - retorna a mensagem retornada pelo DAO
     *
     * O que esse teste prova?
     * - Delete só acontece se o endereço existir e pertencer à fazenda
     */
    @Test
    void shouldDeleteAddressWhenExists() {
        // Arrange
        Long farmId = 1L;
        Long addressId = 10L;

        Address existing = new Address();

        when(addressDAO.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.of(existing));
        when(addressDAO.deleteAddress(addressId)).thenReturn("OK");

        // Act
        String result = addressBusiness.deleteAddress(farmId, addressId);

        // Assert
        assertSame("OK", result);

        // Verify
        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressDAO).findByIdAndFarmId(addressId, farmId);
        verify(addressDAO).deleteAddress(addressId);

        // mapper não tem nada a ver com delete
        verifyNoInteractions(addressMapper);
    }

    /**
     * CENÁRIO 11 (Delete - not found):
     * - ownership é verificado
     * - DAO.findByIdAndFarmId(...) retorna empty
     * - Business lança ResourceNotFoundException
     * - DAO.deleteAddress NÃO é chamado
     *
     * O que esse teste prova?
     * - Não apaga algo que não existe
     * - Evita deletes "às cegas"
     */
    @Test
    void shouldThrowResourceNotFoundWhenDeleteAddressDoesNotExist() {
        // Arrange
        Long farmId = 1L;
        Long addressId = 10L;

        when(addressDAO.findByIdAndFarmId(addressId, farmId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class,
                () -> addressBusiness.deleteAddress(farmId, addressId));

        // Verify
        verify(ownershipService).verifyFarmOwnership(farmId);
        verify(addressDAO).findByIdAndFarmId(addressId, farmId);

        verify(addressDAO, never()).deleteAddress(any());
        verifyNoInteractions(addressMapper);
    }

}
