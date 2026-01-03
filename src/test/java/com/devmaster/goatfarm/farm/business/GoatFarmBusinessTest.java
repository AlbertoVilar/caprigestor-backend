package com.devmaster.goatfarm.farm.business;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.authority.business.usersbusiness.UserBusiness;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.farmbusiness.GoatFarmBusiness;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.phone.business.business.PhoneBusiness;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoatFarmBusinessTest {

    @InjectMocks
    private GoatFarmBusiness goatFarmBusiness;

    @Mock
    private GoatFarmDAO goatFarmDAO;
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
        when(goatFarmDAO.existsByName(any())).thenReturn(false);
        when(goatFarmDAO.existsByTod(any())).thenReturn(false);
        when(addressBusiness.findOrCreateAddressEntity(any())).thenReturn(mockAddress);
        when(goatFarmMapper.toEntity(any())).thenReturn(mockFarm);
        when(goatFarmDAO.save(any())).thenReturn(mockFarm);
        when(goatFarmDAO.findFarmEntityById(any())).thenReturn(mockFarm);
        
        GoatFarmFullResponseVO responseVO = new GoatFarmFullResponseVO();
        when(goatFarmMapper.toFullResponseVO(any())).thenReturn(responseVO);

        GoatFarmFullResponseVO result = goatFarmBusiness.createGoatFarm(fullRequestVO);

        assertNotNull(result);
        verify(ownershipService, atLeastOnce()).getCurrentUser();
        verify(goatFarmDAO).save(any());
        verify(phoneBusiness).createPhones(eq(100L), any());
    }

    @Test
    @DisplayName("Should create farm successfully when anonymous (registration flow)")
    void createGoatFarm_success_anonymous() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        when(userBusiness.findUserByEmail(userVO.getEmail())).thenReturn(Optional.empty());
        when(userBusiness.findOrCreateUser(any())).thenReturn(mockUser);
        when(goatFarmDAO.existsByName(any())).thenReturn(false);
        when(addressBusiness.findOrCreateAddressEntity(any())).thenReturn(mockAddress);
        when(goatFarmMapper.toEntity(any())).thenReturn(mockFarm);
        when(goatFarmDAO.save(any())).thenReturn(mockFarm);
        when(goatFarmDAO.findFarmEntityById(any())).thenReturn(mockFarm);
        when(goatFarmMapper.toFullResponseVO(any())).thenReturn(new GoatFarmFullResponseVO());

        GoatFarmFullResponseVO result = goatFarmBusiness.createGoatFarm(fullRequestVO);

        assertNotNull(result);
        
        // Verifica se a role foi definida corretamente
        ArgumentCaptor<UserRequestVO> userCaptor = ArgumentCaptor.forClass(UserRequestVO.class);
        verify(userBusiness).findOrCreateUser(userCaptor.capture());
        UserRequestVO capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser.getRoles());
        assertTrue(capturedUser.getRoles().contains("ROLE_USER"));
        assertEquals(1, capturedUser.getRoles().size());
        
        verify(goatFarmDAO).save(any());
    }

    @Test
    @DisplayName("Should fail when anonymous providing roles")
    void createGoatFarm_fail_anonymous_with_roles() {
        when(ownershipService.getCurrentUser()).thenThrow(new UnauthorizedException("Anonymous"));
        userVO.setRoles(List.of("ROLE_ADMIN"));
        
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
        when(userBusiness.findUserByEmail(userVO.getEmail())).thenReturn(Optional.of(mockUser));
        when(goatFarmDAO.existsByName(any())).thenReturn(false); // Validations pass first

        DuplicateEntityException exception = assertThrows(DuplicateEntityException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
        
        assertEquals("Não foi possível completar o cadastro com os dados informados.", exception.getMessage());
        
        verify(userBusiness, never()).findOrCreateUser(any());
        verify(goatFarmDAO, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when validation fails (null farm)")
    void createGoatFarm_fail_validation_null_farm() {
        fullRequestVO.setFarm(null);
        assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }

    @Test
    @DisplayName("Should fail when validation fails (null phones)")
    void createGoatFarm_fail_validation_null_phones() {
        fullRequestVO.setPhones(null);
        assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }

    @Test
    @DisplayName("Should fail when validation fails (empty phones)")
    void createGoatFarm_fail_validation_empty_phones() {
        fullRequestVO.setPhones(Collections.emptyList());
        assertThrows(ValidationException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }
    
    @Test
    @DisplayName("Should fail when farm name is duplicate")
    void createGoatFarm_fail_duplicate_name() {
        when(goatFarmDAO.existsByName(farmVO.getName())).thenReturn(true);
        
        assertThrows(DuplicateEntityException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }

    @Test
    @DisplayName("Should fail when farm TOD is duplicate")
    void createGoatFarm_fail_duplicate_tod() {
        when(goatFarmDAO.existsByName(any())).thenReturn(false);
        when(goatFarmDAO.existsByTod(farmVO.getTod())).thenReturn(true);
        
        assertThrows(DuplicateEntityException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }

    @Test
    @DisplayName("Should throw DatabaseException on data integrity violation")
    void createGoatFarm_fail_database_error() {
        when(ownershipService.getCurrentUser()).thenReturn(mockUser);
        when(goatFarmDAO.existsByName(any())).thenReturn(false);
        when(addressBusiness.findOrCreateAddressEntity(any())).thenReturn(mockAddress);
        when(goatFarmMapper.toEntity(any())).thenReturn(mockFarm);
        
        when(goatFarmDAO.save(any())).thenThrow(new DataIntegrityViolationException("Constraint violation"));

        assertThrows(DatabaseException.class, () -> 
            goatFarmBusiness.createGoatFarm(fullRequestVO)
        );
    }
}