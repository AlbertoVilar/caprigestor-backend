package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenealogyBusinessTest {

    @Mock
    private GoatPersistencePort goatPort;

    @Mock
    private GenealogyMapper genealogyMapper;

    @Mock
    private OwnershipService ownershipService;

    @InjectMocks
    private GenealogyBusiness genealogyBusiness;

    private Long farmId;
    private String goatId;
    private Goat mockGoat;
    private GenealogyResponseVO mockResponseVO;

    @BeforeEach
    void setUp() {
        farmId = 1L;
        goatId = "goat-123";
        mockGoat = new Goat();
        mockGoat.setRegistrationNumber(goatId);
        
        mockResponseVO = new GenealogyResponseVO();
        mockResponseVO.setGoatRegistration(goatId);
    }

    @Test
    @DisplayName("Should return GenealogyResponseVO when goat exists")
    void shouldReturnGenealogyResponseVO_WhenGoatExists() {
        // Arrange
        when(goatPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)).thenReturn(Optional.of(mockGoat));
        when(genealogyMapper.toResponseVO(mockGoat)).thenReturn(mockResponseVO);

        // Act
        GenealogyResponseVO result = genealogyBusiness.findGenealogy(farmId, goatId);

        // Assert
        assertNotNull(result);
        assertEquals(goatId, result.getGoatRegistration());
        
        verify(goatPort).findByIdAndFarmIdWithFamilyGraph(goatId, farmId);
        verify(genealogyMapper).toResponseVO(mockGoat);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when goat does not exist")
    void shouldThrowResourceNotFoundException_WhenGoatDoesNotExist() {
        // Arrange
        when(goatPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> genealogyBusiness.findGenealogy(farmId, goatId));
        
        verify(goatPort).findByIdAndFarmIdWithFamilyGraph(goatId, farmId);
        verifyNoInteractions(genealogyMapper);
    }

    @Test
    @DisplayName("Should return projection even for createGenealogy (Deprecated)")
    void shouldReturnProjection_WhenCreateGenealogyCalled() {
        // Arrange
        // createGenealogy calls verifyGoatOwnership first, then findGenealogy
        
        // Mock findGenealogy behavior (since it's the same class method call, we can't easily mock internal call unless we use spy, 
        // but here we just rely on the underlying dependencies of findGenealogy being called again)
        
        when(goatPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)).thenReturn(Optional.of(mockGoat));
        when(genealogyMapper.toResponseVO(mockGoat)).thenReturn(mockResponseVO);

        // Act
        GenealogyResponseVO result = genealogyBusiness.createGenealogy(farmId, goatId);

        // Assert
        assertNotNull(result);
        assertEquals(goatId, result.getGoatRegistration());
        
        verify(ownershipService).verifyGoatOwnership(farmId, goatId);
        verify(goatPort).findByIdAndFarmIdWithFamilyGraph(goatId, farmId);
        verify(genealogyMapper).toResponseVO(mockGoat);
    }
}
