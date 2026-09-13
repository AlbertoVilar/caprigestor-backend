package com.devmaster.goatfarm.phone.business.phoneservice;

import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.phone.application.ports.out.PhonePersistencePort;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;

@ExtendWith(MockitoExtension.class)
class PhoneBusinessTest {
    @Mock PhonePersistencePort phonePort; @Mock FarmAuthorizationUseCase authorization;
    private final PhoneRequestVO valid = new PhoneRequestVO(null,"11","999999999",null);
    @Test void deleteLastPhoneFails() { var b=new PhoneBusiness(phonePort,authorization); when(phonePort.findByIdAndFarmId(10L,1L)).thenReturn(Optional.of(new PhoneResponseVO(10L,"11","999999999"))); when(phonePort.countByFarmId(1L)).thenReturn(1L); assertThrows(BusinessRuleException.class,()->b.deletePhone(1L,10L)); verify(phonePort,never()).deleteById(anyLong()); }
    @Test void createDelegatesCleanData() { var b=new PhoneBusiness(phonePort,authorization); when(phonePort.existsByDddAndNumber(anyString(),anyString())).thenReturn(false); when(phonePort.save(eq(1L),any())).thenReturn(new PhoneResponseVO(1L,"11","999999999")); b.createPhone(1L,valid); verify(phonePort).save(eq(1L),any()); }
}
