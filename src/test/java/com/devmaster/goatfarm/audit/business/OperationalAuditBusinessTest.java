package com.devmaster.goatfarm.audit.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.out.OperationalAuditPersistencePort;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalAuditBusinessTest {

    @Mock
    private OperationalAuditPersistencePort operationalAuditPersistencePort;
    @Mock
    private GoatFarmPersistencePort goatFarmPersistencePort;
    @Mock
    private OwnershipService ownershipService;
    @Mock
    private EntityFinder entityFinder;

    private OperationalAuditBusiness operationalAuditBusiness;

    @BeforeEach
    void setUp() {
        operationalAuditBusiness = new OperationalAuditBusiness(
                operationalAuditPersistencePort,
                goatFarmPersistencePort,
                ownershipService,
                entityFinder
        );

        lenient().when(entityFinder.findOrThrow(any(), anyString())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Supplier<Optional<Object>> supplier = invocation.getArgument(0);
            String message = invocation.getArgument(1);
            return supplier.get().orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException(message));
        });
    }

    @Test
    void shouldRecordAuditEntryWithAuthenticatedActor() {
        GoatFarm farm = new GoatFarm();
        farm.setId(1L);

        User currentUser = new User();
        currentUser.setId(7L);
        currentUser.setName("Operador QA");
        currentUser.setEmail("operator@example.com");

        when(goatFarmPersistencePort.findById(1L)).thenReturn(Optional.of(farm));
        when(ownershipService.getCurrentUser()).thenReturn(currentUser);
        when(operationalAuditPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        operationalAuditBusiness.record(new OperationalAuditRecordVO(
                1L,
                "G-001",
                OperationalAuditActionType.ANIMAL_SALE_CREATED,
                "99",
                "Venda auditada"
        ));

        ArgumentCaptor<OperationalAuditEntry> captor = ArgumentCaptor.forClass(OperationalAuditEntry.class);
        verify(operationalAuditPersistencePort).save(captor.capture());
        assertEquals("G-001", captor.getValue().getGoatRegistrationNumber());
        assertEquals(OperationalAuditActionType.ANIMAL_SALE_CREATED, captor.getValue().getActionType());
        assertEquals(7L, captor.getValue().getActorUserId());
        assertEquals("Operador QA", captor.getValue().getActorName());
    }

    @Test
    void shouldListEntriesByGoatWithNormalizedLimit() {
        GoatFarm farm = new GoatFarm();
        farm.setId(1L);

        OperationalAuditEntry entry = OperationalAuditEntry.builder()
                .id(5L)
                .farm(farm)
                .goatRegistrationNumber("G-001")
                .actionType(OperationalAuditActionType.GOAT_EXIT)
                .targetId("G-001")
                .actorUserId(7L)
                .actorName("Operador QA")
                .actorEmail("operator@example.com")
                .description("Saida auditada")
                .build();

        when(goatFarmPersistencePort.findById(1L)).thenReturn(Optional.of(farm));
        when(operationalAuditPersistencePort.findByFarmIdAndGoatRegistrationNumber(1L, "G-001", 15)).thenReturn(List.of(entry));

        var result = operationalAuditBusiness.listEntries(1L, " G-001 ", 0);

        assertEquals(1, result.size());
        assertEquals("Saida do rebanho", result.get(0).actionLabel());
        assertEquals("Saida auditada", result.get(0).description());
    }
}
