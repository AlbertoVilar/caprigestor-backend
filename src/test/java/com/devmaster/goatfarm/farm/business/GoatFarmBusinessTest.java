package com.devmaster.goatfarm.farm.business;

import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.persistence.entity.Address;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.GoatFarmBusiness;
import com.devmaster.goatfarm.farm.api.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoatFarmBusinessTest {

    private GoatFarmBusiness goatFarmBusiness;

    @Mock
    private GoatFarmPersistencePort goatFarmPort;
    @Mock
    private GoatFarmMapper goatFarmMapper;
    @Mock
    private OwnershipService ownershipService;
    @Mock
    private UserBusiness userBusiness;
    @Mock
    private AddressBusiness addressBusiness;
    @Mock
    private PhoneBusiness phoneBusiness;
    @Mock
    private PhoneMapper phoneMapper;

    private GoatFarmRequestVO farmVO;
    private UserRequestVO userVO;
    private AddressRequestVO addressVO;
    private List<PhoneRequestVO> phoneVOs;
    private GoatFarmFullRequestVO fullRequestVO;
    private User mockUser;
    private Address mockAddress;
    private GoatFarm mockFarm;

    @BeforeEach
    void setUp() {
        goatFarmBusiness = new GoatFarmBusiness(
                goatFarmPort,
                addressBusiness,
                userBusiness,
                phoneBusiness,
                goatFarmMapper,
                phoneMapper,
                ownershipService
        );

        farmVO = new GoatFarmRequestVO();
        farmVO.setName("Test Farm");
        farmVO.setTod("TEST01");

        userVO = new UserRequestVO();
        userVO.setEmail("test@example.com");

        addressVO = new AddressRequestVO();
        
        PhoneRequestVO phoneVO = new PhoneRequestVO();
        phoneVOs = List.of(phoneVO);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        mockAddress = new Address();
        mockAddress.setId(1L);

        mockFarm = new GoatFarm();
        mockFarm.setId(100L);

        fullRequestVO = new GoatFarmFullRequestVO(farmVO, userVO, addressVO, phoneVOs);
    }

    @Test
    @DisplayName("Should create farm successfully when user is authenticated")
    void createGoatFarm_success_authenticated() {
        when(ownershipService.getCurrentUser()).thenReturn(mockUser);
        when(goatFarmPort.existsByName(any())).thenReturn(false);
        when(goatFarmPort.existsByTod(any())).thenReturn(false);
        when(addressBusiness.findOrCreateAddressEntity(any())).thenReturn(mockAddress);
        when(goatFarmMapper.toEntity(any())).thenReturn(mockFarm);
        when(goatFarmPort.save(any())).thenReturn(mockFarm);
        when(goatFarmPort.findById(any())).thenReturn(Optional.of(mockFarm));
        
        GoatFarmFullResponseVO responseVO = new GoatFarmFullResponseVO();
        when(goatFarmMapper.toFullResponseVO(any())).thenReturn(responseVO);

        GoatFarmFullResponseVO result = goatFarmBusiness.createGoatFarm(fullRequestVO);

        assertNotNull(result);
        verify(ownershipService, atLeastOnce()).getCurrentUser();
        
        ArgumentCaptor<GoatFarm> farmCaptor = ArgumentCaptor.forClass(GoatFarm.class);
        verify(goatFarmPort).save(farmCaptor.capture());
        GoatFarm capturedFarm = farmCaptor.getValue();
        assertEquals(mockUser, capturedFarm.getUser());
        assertEquals(mockAddress, capturedFarm.getAddress());
        
        verify(phoneBusiness).createPhones(eq(100L), any());
    }

    @Test
    @DisplayName("Should create farm successfully when anonymous (registration flow)")
    void createGoatFarm_success_anonymous() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        when(userBusiness.findUserByEmail(userVO.getEmail())).thenReturn(java.util.Optional.empty());
        when(userBusiness.findOrCreateUser(any())).thenReturn(mockUser);
        when(goatFarmPort.existsByName(any())).thenReturn(false);
        when(addressBusiness.findOrCreateAddressEntity(any())).thenReturn(mockAddress);
        when(goatFarmMapper.toEntity(any())).thenReturn(mockFarm);
        when(goatFarmPort.save(any())).thenReturn(mockFarm);
        when(goatFarmPort.findById(any())).thenReturn(Optional.of(mockFarm));
        when(goatFarmMapper.toFullResponseVO(any())).thenReturn(new GoatFarmFullResponseVO());

        GoatFarmFullResponseVO result = goatFarmBusiness.createGoatFarm(fullRequestVO);

        assertNotNull(result);
        verify(ownershipService, times(1)).getCurrentUser();
        
        // Verifica se a role foi definida corretamente
        ArgumentCaptor<UserRequestVO> userCaptor = ArgumentCaptor.forClass(UserRequestVO.class);
        verify(userBusiness).findOrCreateUser(userCaptor.capture());
        UserRequestVO capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser.getRoles());
        assertTrue(capturedUser.getRoles().contains("ROLE_FARM_OWNER"));
        assertEquals(1, capturedUser.getRoles().size());
        
        verify(goatFarmPort).save(any());
    }

    @Test
    @DisplayName("Should fail when anonymous providing roles")
    void createGoatFarm_fail_anonymous_with_roles() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        userVO.setRoles(java.util.List.of("ROLE_ADMIN"));
        
        assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
        
        verify(userBusiness, never()).findOrCreateUser(any());
    }

    @Test
    @DisplayName("Should fail when anonymous providing null user")
    void createGoatFarm_fail_anonymous_no_user() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        fullRequestVO.setUser(null);
        
        ValidationException exception = assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
        assertTrue(exception.getMessage().contains("Erro de validação"));
    }

    @Test
    @DisplayName("Should fail with accumulated validation errors")
    void createGoatFarm_fail_validation_accumulated() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        fullRequestVO.setUser(null);
        fullRequestVO.setFarm(null);
        fullRequestVO.setPhones(null);
        
        ValidationException exception = assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
        
        // Deve conter erros de farm, user e phones
        ValidationError errors = exception.getValidationError();
        assertEquals(3, errors.getErrors().size());
    }

    @Test
    @DisplayName("Should fail when anonymous and user already exists")
    void createGoatFarm_fail_anonymous_existing_user() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        when(userBusiness.findUserByEmail(userVO.getEmail())).thenReturn(java.util.Optional.of(mockUser));
        when(goatFarmPort.existsByName(any())).thenReturn(false); // Validations pass first

        DuplicateEntityException exception = assertThrows(DuplicateEntityException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
        
        assertEquals("Não foi possível completar o cadastro com os dados informados.", exception.getMessage());
        
        verify(userBusiness, never()).findOrCreateUser(any());
        verify(goatFarmPort, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when validation fails (null farm)")
    void createGoatFarm_fail_validation_null_farm() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        fullRequestVO.setFarm(null);
        assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }

    @Test
    @DisplayName("Should fail when validation fails (null phones)")
    void createGoatFarm_fail_validation_null_phones() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        fullRequestVO.setPhones(null);
        assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }

    @Test
    @DisplayName("Should fail when validation fails (empty phones)")
    void createGoatFarm_fail_validation_empty_phones() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        fullRequestVO.setPhones(Collections.emptyList());
        assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }
    
    @Test
    @DisplayName("Should fail when farm name is duplicate")
    void createGoatFarm_fail_duplicate_name() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        when(goatFarmPort.existsByName(farmVO.getName())).thenReturn(true);
        
        assertThrows(DuplicateEntityException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
        
        verify(goatFarmPort, never()).save(any());
        verify(phoneBusiness, never()).createPhones(anyLong(), any());
    }

    @Test
    @DisplayName("Should fail when farm TOD is duplicate")
    void createGoatFarm_fail_duplicate_tod() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        when(goatFarmPort.existsByName(any())).thenReturn(false);
        when(goatFarmPort.existsByTod(farmVO.getTod())).thenReturn(true);
        
        assertThrows(DuplicateEntityException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
        
        verify(goatFarmPort, never()).save(any());
        verify(phoneBusiness, never()).createPhones(anyLong(), any());
    }

    @Test
    @DisplayName("Should throw DatabaseException on data integrity violation")
    void createGoatFarm_fail_database_error() {
        when(ownershipService.getCurrentUser()).thenReturn(mockUser);
        when(goatFarmPort.existsByName(any())).thenReturn(false);
        when(goatFarmPort.existsByTod(any())).thenReturn(false);
        when(addressBusiness.findOrCreateAddressEntity(any())).thenReturn(mockAddress);
        when(goatFarmMapper.toEntity(any())).thenReturn(mockFarm);
        
        when(goatFarmPort.save(any())).thenThrow(new DataIntegrityViolationException("Constraint violation"));

        assertThrows(DatabaseException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }

    @Test
    @DisplayName("Should update farm without requiring password and without changing owner")
    void updateGoatFarm_success_withoutPassword() {
        mockFarm.setUser(mockUser);

        when(goatFarmPort.findById(100L)).thenReturn(Optional.of(mockFarm));
        when(goatFarmPort.save(any())).thenReturn(mockFarm);
        when(goatFarmPort.findById(mockFarm.getId())).thenReturn(Optional.of(mockFarm));
        when(goatFarmMapper.toFullResponseVO(any())).thenReturn(new GoatFarmFullResponseVO());
        when(addressBusiness.findOrCreateAddressEntity(any())).thenReturn(mockAddress);
        when(userBusiness.updateUser(eq(1L), any(UserRequestVO.class)))
                .thenReturn(new UserResponseVO(1L, "Owner", "owner@example.com", "00000000001", java.util.List.of()));

        GoatFarmFullResponseVO result = goatFarmBusiness.updateGoatFarm(100L, farmVO, userVO, addressVO, phoneVOs);

        assertNotNull(result);
        verify(userBusiness).updateUser(eq(1L), any(UserRequestVO.class));
        verify(userBusiness, never()).findOrCreateUser(any());
        verify(phoneBusiness).replacePhones(eq(100L), eq(phoneVOs));
    }

    @Test
    @DisplayName("Should fail when trying to update farm without an existing owner")
    void updateGoatFarm_fail_withoutOwner() {
        mockFarm.setUser(null);

        when(goatFarmPort.findById(100L)).thenReturn(Optional.of(mockFarm));

        assertThrows(ValidationException.class, () ->
                goatFarmBusiness.updateGoatFarm(100L, farmVO, userVO, addressVO, phoneVOs)
        );

        verify(userBusiness, never()).findOrCreateUser(any());
        verify(userBusiness, never()).updateUser(anyLong(), any());
    }
}
