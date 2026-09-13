package com.devmaster.goatfarm.farm.business;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.authority.application.ports.in.*;
import com.devmaster.goatfarm.authority.business.bo.*;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.business.bo.*;
import com.devmaster.goatfarm.farm.business.mapper.FarmBusinessMapper;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.phoneservice.PhoneBusiness;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoatFarmBusinessTest {
    @Mock GoatFarmPersistencePort farmPort; @Mock AddressBusiness addressBusiness; @Mock UserManagementUseCase users; @Mock PhoneBusiness phones; @Mock FarmBusinessMapper mapper; @Mock FarmAuthorizationUseCase auth; @Mock CurrentPrincipalQueryUseCase principal;
    private GoatFarmBusiness business() { return new GoatFarmBusiness(farmPort,addressBusiness,users,phones,mapper,auth,principal); }
    private GoatFarmFullRequestVO request() { GoatFarmRequestVO f=new GoatFarmRequestVO(); f.setName("Farm"); f.setTod("12345"); AddressRequestVO a=new AddressRequestVO(); PhoneRequestVO p=new PhoneRequestVO(null,"11","999999999",null); return new GoatFarmFullRequestVO(f,null,a,List.of(p)); }
    private FarmRecord record() { return new FarmRecord(1L,"Farm","12345",null,new com.devmaster.goatfarm.farm.application.model.OwnerReference(7L,"Owner","o@x","12345678901",List.of("ROLE_FARM_OWNER")),new AddressResponseVO(2L,null,null,null,null,null,null),List.of(new com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO(3L,"11","999999999")),null,null,0); }
    @Test void authenticatedCreateUsesCleanPorts() { when(principal.findCurrent()).thenReturn(Optional.of(new AuthenticatedPrincipal(7L,"o@x","Owner",Set.of("ROLE_FARM_OWNER")))); when(farmPort.existsByName(any())).thenReturn(false); when(farmPort.existsByTod(any())).thenReturn(false); when(addressBusiness.findOrCreateAddress(any())).thenReturn(new AddressResponseVO(2L,null,null,null,null,null,null)); when(farmPort.save(any())).thenReturn(record()); when(farmPort.findByIdWithDetails(1L)).thenReturn(Optional.of(record())); when(mapper.toFullResponseVO(any())).thenReturn(new GoatFarmFullResponseVO()); assertNotNull(business().createGoatFarm(request())); verify(farmPort).save(any()); verify(phones).createPhones(eq(1L),any()); }
    @Test void duplicateNameIsRejected() { when(principal.findCurrent()).thenReturn(Optional.of(new AuthenticatedPrincipal(7L,"o@x","Owner",Set.of()))); when(farmPort.existsByName("Farm")).thenReturn(true); assertThrows(RuntimeException.class,()->business().createGoatFarm(request())); }
}
