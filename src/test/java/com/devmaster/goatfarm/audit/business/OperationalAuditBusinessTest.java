package com.devmaster.goatfarm.audit.business;

import com.devmaster.goatfarm.audit.application.model.OperationalAuditRecord;
import com.devmaster.goatfarm.audit.application.ports.out.OperationalAuditPersistencePort;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalAuditBusinessTest {

    @Mock
    private OperationalAuditPersistencePort operationalAuditPersistencePort;
    @Mock
    private GoatFarmPersistencePort goatFarmPersistencePort;
    @Mock private CurrentPrincipalQueryUseCase currentPrincipalQuery;

    private OperationalAuditBusiness operationalAuditBusiness;

    @BeforeEach
    void setUp() {
        operationalAuditBusiness = new OperationalAuditBusiness(
                operationalAuditPersistencePort,
                goatFarmPersistencePort,
                null,
                currentPrincipalQuery
        );
    }

    @Test
    void shouldRecordAuditEntryWithAuthenticatedActor() {
        AuthenticatedPrincipal currentUser = new AuthenticatedPrincipal(
                7L, "operator@example.com", "Operador QA", Set.of());

        when(goatFarmPersistencePort.findById(1L)).thenReturn(Optional.of(farmRecord()));
        when(currentPrincipalQuery.requireCurrent()).thenReturn(currentUser);
        when(operationalAuditPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        operationalAuditBusiness.record(new OperationalAuditRecordVO(
                1L,
                "G-001",
                OperationalAuditActionType.ANIMAL_SALE_CREATED,
                "99",
                "Venda auditada"
        ));

        ArgumentCaptor<OperationalAuditRecord> captor = ArgumentCaptor.forClass(OperationalAuditRecord.class);
        verify(operationalAuditPersistencePort).save(captor.capture());
        assertEquals("G-001", captor.getValue().goatRegistrationNumber());
        assertEquals(OperationalAuditActionType.ANIMAL_SALE_CREATED, captor.getValue().actionType());
        assertEquals(7L, captor.getValue().actorUserId());
        assertEquals("Operador QA", captor.getValue().actorName());
    }

    @Test
    void shouldListEntriesByGoatWithNormalizedLimit() {
        OperationalAuditRecord entry = new OperationalAuditRecord(
                5L, 1L, null, "G-001", OperationalAuditActionType.GOAT_EXIT,
                "G-001", 7L, "Operador QA", "operator@example.com",
                "Saida auditada", null);

        when(goatFarmPersistencePort.findById(1L)).thenReturn(Optional.of(farmRecord()));
        when(operationalAuditPersistencePort.findByFarmIdAndGoatRegistrationNumber(1L, "G-001", 15)).thenReturn(List.of(entry));

        var result = operationalAuditBusiness.listEntries(1L, " G-001 ", 0);

        assertEquals(1, result.size());
        assertEquals("Saida do rebanho", result.get(0).actionLabel());
        assertEquals("Saida auditada", result.get(0).description());
    }

    private FarmRecord farmRecord() {
        return new FarmRecord(1L, "Fazenda QA", null, null, null, null, java.util.List.of(), null, null, null);
    }
}
