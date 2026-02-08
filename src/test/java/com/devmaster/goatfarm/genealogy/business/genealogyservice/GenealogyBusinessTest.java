package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogyQueryPort;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.business.mapper.GenealogyBusinessMapper;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
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
    private GoatGenealogyQueryPort goatGenealogyQueryPort;

    @Mock
    private GenealogyBusinessMapper genealogyMapper;

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
        
        mockResponseVO = GenealogyResponseVO.builder()
                .goatRegistration(goatId)
                .build();
    }

    @Test
    @DisplayName("Should return GenealogyResponseVO when goat exists")
    void shouldReturnGenealogyResponseVO_WhenGoatExists() {
        // Arrange
        when(goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)).thenReturn(Optional.of(mockGoat));
        when(genealogyMapper.toResponseVO(mockGoat)).thenReturn(mockResponseVO);

        // Act
        GenealogyResponseVO result = genealogyBusiness.findGenealogy(farmId, goatId);

        // Assert
        assertNotNull(result);
        assertEquals(goatId, result.getGoatRegistration());
        
        verify(goatGenealogyQueryPort).findByIdAndFarmIdWithFamilyGraph(goatId, farmId);
        verify(genealogyMapper).toResponseVO(mockGoat);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when goat does not exist")
    void shouldThrowResourceNotFoundException_WhenGoatDoesNotExist() {
        // Arrange
        when(goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> genealogyBusiness.findGenealogy(farmId, goatId));
        
        verify(goatGenealogyQueryPort).findByIdAndFarmIdWithFamilyGraph(goatId, farmId);
        verifyNoInteractions(genealogyMapper);
    }
}
