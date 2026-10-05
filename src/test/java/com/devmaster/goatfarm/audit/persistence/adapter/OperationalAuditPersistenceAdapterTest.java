package com.devmaster.goatfarm.audit.persistence.adapter;

import com.devmaster.goatfarm.audit.application.model.OperationalAuditRecord;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.audit.persistence.entity.OperationalAuditEntry;
import com.devmaster.goatfarm.audit.persistence.repository.OperationalAuditEntryRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalAuditPersistenceAdapterTest {

    @Mock
    private OperationalAuditEntryRepository entryRepository;
    @Mock
    private GoatFarmRepository farmRepository;

    private OperationalAuditPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OperationalAuditPersistenceAdapter(entryRepository, farmRepository);
    }

    @Test
    void saveMapsApplicationSnapshotAndResolvesFarmOnlyInsideAdapter() {
        GoatFarm farm = new GoatFarm();
        farm.setId(9L);
        when(farmRepository.getReferenceById(9L)).thenReturn(farm);
        when(entryRepository.save(any(OperationalAuditEntry.class))).thenAnswer(invocation -> {
            OperationalAuditEntry entry = invocation.getArgument(0);
            entry.setId(21L);
            return entry;
        });

        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 12, 22, 0);
        OperationalAuditRecord result = adapter.save(new OperationalAuditRecord(
                null, 9L, 77L, "RG-77", OperationalAuditActionType.GOAT_EXIT,
                "target-1", 4L, "Operator", "operator@example.test", "Exited", createdAt));

        ArgumentCaptor<OperationalAuditEntry> captor = ArgumentCaptor.forClass(OperationalAuditEntry.class);
        verify(entryRepository).save(captor.capture());
        OperationalAuditEntry persisted = captor.getValue();
        assertEquals(farm, persisted.getFarm());
        assertEquals(77L, persisted.getGoatTechnicalId());
        assertEquals("RG-77", persisted.getGoatRegistrationNumber());
        assertEquals(21L, result.id());
        assertEquals(9L, result.farmId());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    void listMapsLegacyRegistrationOnlySnapshotWithoutGoatId() {
        GoatFarm farm = new GoatFarm();
        farm.setId(9L);
        OperationalAuditEntry entry = OperationalAuditEntry.builder()
                .id(21L).farm(farm).goatRegistrationNumber("LEGACY-RG")
                .actionType(OperationalAuditActionType.GOAT_EXIT)
                .actorUserId(4L).actorName("Operator").actorEmail("operator@example.test")
                .description("Legacy").build();
        when(entryRepository.findByFarm_IdAndGoatRegistrationNumberOrderByCreatedAtDescIdDesc(any(), any(), any()))
                .thenReturn(List.of(entry));

        List<OperationalAuditRecord> result = adapter.findByFarmIdAndGoatRegistrationNumber(9L, "LEGACY-RG", 15);

        assertEquals(1, result.size());
        assertEquals("LEGACY-RG", result.get(0).goatRegistrationNumber());
        assertEquals(null, result.get(0).goatTechnicalId());
        assertEquals(9L, result.get(0).farmId());
    }
}
