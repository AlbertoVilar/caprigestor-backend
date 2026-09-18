package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.genealogy.application.model.GenealogyNodeSource;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;
import com.devmaster.goatfarm.goat.domain.GoatId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenealogyTreeProjectionBusinessTest {

    @Mock
    private GenealogyAbccQueryPort genealogyAbccQueryPort;

    private GenealogyTreeProjectionBusiness service;

    @BeforeEach
    void setUp() {
        service = new GenealogyTreeProjectionBusiness(genealogyAbccQueryPort);
    }

    @Test
    void projectLocal_localNodeUsesTechnicalIdentity() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyTreeSnapshot result = service.projectLocal(root);

        assertThat(result.animalPrincipal().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.animalPrincipal().localGoatId()).isEqualTo(GoatId.of(42L));
        assertThat(result.animalPrincipal().registrationNumber()).isEqualTo("RG-42");
        assertThat(result.integration()).isNull();
    }

    @Test
    void projectLocal_declaredParentPreserved() {
        GoatGenealogySnapshot.ParentReference externalFather = GoatGenealogySnapshot.ParentReference.external("EXT-123");
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, externalFather, null
        );

        GenealogyTreeSnapshot result = service.projectLocal(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.pai().registrationNumber()).isEqualTo("EXT-123");
        assertThat(result.pai().localGoatId()).isNull();
    }

    @Test
    void projectLocal_absentNodeExplicit() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyTreeSnapshot result = service.projectLocal(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
        assertThat(result.pai().name()).isNull();
        assertThat(result.pai().registrationNumber()).isNull();
        assertThat(result.pai().localGoatId()).isNull();
    }

    @Test
    void projectLocal_declaredPaternalGrandfather_isDeclarado() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "RG-PAI", "Pai Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("RG-GP-001"), null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyTreeSnapshot result = service.projectLocal(root);

        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoPaterno().registrationNumber()).isEqualTo("RG-GP-001");
        assertThat(result.avoPaterno().localGoatId()).isNull();
    }

    @Test
    void projectLocal_declaredPaternalGrandmother_isDeclarado() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "RG-PAI", "Pai Local", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.external("RG-GM-001")
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyTreeSnapshot result = service.projectLocal(root);

        assertThat(result.avoPaterna().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoPaterna().registrationNumber()).isEqualTo("RG-GM-001");
    }

    @Test
    void projectLocal_declaredMaternalGrandfather_isDeclarado() {
        GoatGenealogySnapshot mother = new GoatGenealogySnapshot(
                GoatId.of(20L), "RG-MAE", "Mãe Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("RG-MAT-GP-001"), null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.local(mother)
        );

        GenealogyTreeSnapshot result = service.projectLocal(root);

        assertThat(result.avoMaterno().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoMaterno().registrationNumber()).isEqualTo("RG-MAT-GP-001");
    }

    @Test
    void projectLocal_declaredMaternalGrandmother_isDeclarado() {
        GoatGenealogySnapshot mother = new GoatGenealogySnapshot(
                GoatId.of(20L), "RG-MAE", "Mãe Local", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.external("RG-MAT-GM-001")
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.local(mother)
        );

        GenealogyTreeSnapshot result = service.projectLocal(root);

        assertThat(result.avoMaterna().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoMaterna().registrationNumber()).isEqualTo("RG-MAT-GM-001");
    }

    @Test
    void projectLocal_declaredGreatGrandparent_isDeclarado() {
        GoatGenealogySnapshot avoPaterno = new GoatGenealogySnapshot(
                GoatId.of(100L), "RG-AVO", "Avô Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("RG-GGP-001"), null
        );
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "RG-PAI", "Pai Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(avoPaterno), null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyTreeSnapshot result = service.projectLocal(root);

        assertThat(result.bisavoPaternoPai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.bisavoPaternoPai().registrationNumber()).isEqualTo("RG-GGP-001");
    }

    @Test
    void complement_abccOnlyFather_preservesNameAndRegistration() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setFatherName("ALPHA");
        rootAbcc.setFatherRegistrationNumber("RG-ALPHA");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.pai().name()).isEqualTo("ALPHA");
        assertThat(result.pai().registrationNumber()).isEqualTo("RG-ALPHA");
        assertThat(result.pai().localGoatId()).isNull();
    }

    @Test
    void complement_abccOnlyMother_preservesNameAndRegistration() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setMotherName("BETA");
        rootAbcc.setMotherRegistrationNumber("RG-BETA");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.mae().name()).isEqualTo("BETA");
        assertThat(result.mae().registrationNumber()).isEqualTo("RG-BETA");
        assertThat(result.mae().localGoatId()).isNull();
    }

    @Test
    void complement_abccOnlyFather_withRegistrationOnly_isAbccNode() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setFatherRegistrationNumber("RG-ONLY-FATHER");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.pai().name()).isNull();
        assertThat(result.pai().registrationNumber()).isEqualTo("RG-ONLY-FATHER");
    }

    @Test
    void complement_abccOnlyMother_withRegistrationOnly_isAbccNode() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setMotherRegistrationNumber("RG-ONLY-MOTHER");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.mae().name()).isNull();
        assertThat(result.mae().registrationNumber()).isEqualTo("RG-ONLY-MOTHER");
    }

    @Test
    void complement_declaredGrandparent_preservesDeclaredRegistration() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "RG-PAI", "Pai Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DEC-GRANDPA-99"), null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-PAI")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DEC-GRANDPA-99")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoPaterno().registrationNumber()).isEqualTo("DEC-GRANDPA-99");
    }

    @Test
    void complement_declaredGreatGrandparent_preservesDeclaredRegistration() {
        GoatGenealogySnapshot avoPaterno = new GoatGenealogySnapshot(
                GoatId.of(100L), "RG-AVO", "Avô Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DEC-GGP-999"), null
        );
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "RG-PAI", "Pai Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(avoPaterno), null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-PAI")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-AVO")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DEC-GGP-999")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.bisavoPaternoPai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.bisavoPaternoPai().registrationNumber()).isEqualTo("DEC-GGP-999");
    }

    @Test
    void declaredFather_conflictingRootBranch_stillProtected() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DECLARED-001"), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setFatherRegistrationNumber("OTHER-999");
        rootAbcc.setFatherName("OTHER FATHER");
        rootAbcc.setPaternalGrandfatherName("OTHER GP");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("OTHER-GP-999");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DECLARED-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().registrationNumber()).isEqualTo("DECLARED-001");
        assertThat(result.pai().name()).isNull();
        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
        assertThat(result.avoPaterno().name()).isNull();
    }

    @Test
    void declaredMother_conflictingRootAbccParent_doesNotReplaceNameOrRegistration() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.external("DECLARED-MOM-001")
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setMotherRegistrationNumber("OTHER-MOM-999");
        rootAbcc.setMotherName("OTHER MOTHER");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DECLARED-MOM-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().registrationNumber()).isEqualTo("DECLARED-MOM-001");
        assertThat(result.mae().name()).isNull();
        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
    }

    @Test
    void declaredGrandparent_conflictingRootAbccBranch_doesNotReplaceDeclaration() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "RG-PAI", "Pai Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DECLARED-GP-001"), null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("OTHER-GP-999");
        rootAbcc.setPaternalGrandfatherName("OTHER GRANDPA");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-PAI")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DECLARED-GP-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.avoPaterno().registrationNumber()).isEqualTo("DECLARED-GP-001");
        assertThat(result.avoPaterno().name()).isNull();
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
    }

    @Test
    void declaredParent_dedicatedAbccLookup_enrichesCorrectBranch() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DECLARED-FATHER-77"), null
        );

        GenealogyAbccSnapshotVO fatherAbcc = new GenealogyAbccSnapshotVO();
        fatherAbcc.setAnimalRegistrationNumber("DECLARED-FATHER-77");
        fatherAbcc.setAnimalName("Pai Enriquecido Dedicado");
        fatherAbcc.setFatherName("Avô Pelo Pai Dedicado");
        fatherAbcc.setFatherRegistrationNumber("AVO-P-77");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DECLARED-FATHER-77")).thenReturn(Optional.of(fatherAbcc));

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.pai().name()).isEqualTo("Pai Enriquecido Dedicado");
        assertThat(result.pai().registrationNumber()).isEqualTo("DECLARED-FATHER-77");

        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.avoPaterno().name()).isEqualTo("Avô Pelo Pai Dedicado");
        assertThat(result.avoPaterno().registrationNumber()).isEqualTo("AVO-P-77");
    }

    @Test
    void declaredGrandparent_dedicatedAbccLookup_enrichesCorrectBranch() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "RG-PAI", "Pai Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DECLARED-GP-88"), null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyAbccSnapshotVO gpAbcc = new GenealogyAbccSnapshotVO();
        gpAbcc.setAnimalRegistrationNumber("DECLARED-GP-88");
        gpAbcc.setAnimalName("Avô Enriquecido Dedicado");
        gpAbcc.setFatherName("Bisavô Dedicado");
        gpAbcc.setFatherRegistrationNumber("BIS-88");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-PAI")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DECLARED-GP-88")).thenReturn(Optional.of(gpAbcc));

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoPaterno().name()).isEqualTo("Avô Enriquecido Dedicado");
        assertThat(result.avoPaterno().registrationNumber()).isEqualTo("DECLARED-GP-88");

        assertThat(result.bisavoPaternoPai().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.bisavoPaternoPai().name()).isEqualTo("Bisavô Dedicado");
        assertThat(result.bisavoPaternoPai().registrationNumber()).isEqualTo("BIS-88");
    }

    @Test
    void rootFallback_allEightGreatGrandparents_useExactFields() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyAbccSnapshotVO abcc = new GenealogyAbccSnapshotVO();
        abcc.setAnimalRegistrationNumber("RG-42");

        abcc.setBisavoPaternoPaiName("BPP_Name");
        abcc.setBisavoPaternoPaiRegistrationNumber("BPP_Reg");

        abcc.setBisavoPaternaPaiName("BPA_P_Name");
        abcc.setBisavoPaternaPaiRegistrationNumber("BPA_P_Reg");

        abcc.setBisavoPaternoMaeName("BPM_Name");
        abcc.setBisavoPaternoMaeRegistrationNumber("BPM_Reg");

        abcc.setBisavoPaternaMaeName("BPA_M_Name");
        abcc.setBisavoPaternaMaeRegistrationNumber("BPA_M_Reg");

        abcc.setBisavoMaternoPaiName("BMP_Name");
        abcc.setBisavoMaternoPaiRegistrationNumber("BMP_Reg");

        abcc.setBisavoMaternaPaiName("BMA_P_Name");
        abcc.setBisavoMaternaPaiRegistrationNumber("BMA_P_Reg");

        abcc.setBisavoMaternoMaeName("BMM_Name");
        abcc.setBisavoMaternoMaeRegistrationNumber("BMM_Reg");

        abcc.setBisavoMaternaMaeName("BMA_M_Name");
        abcc.setBisavoMaternaMaeRegistrationNumber("BMA_M_Reg");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(abcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("BPP_Reg")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("BPA_P_Reg")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("BPM_Reg")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("BPA_M_Reg")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("BMP_Reg")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("BMA_P_Reg")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("BMM_Reg")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("BMA_M_Reg")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.bisavoPaternoPai().name()).isEqualTo("BPP_Name");
        assertThat(result.bisavoPaternoPai().registrationNumber()).isEqualTo("BPP_Reg");

        assertThat(result.bisavoPaternaPai().name()).isEqualTo("BPA_P_Name");
        assertThat(result.bisavoPaternaPai().registrationNumber()).isEqualTo("BPA_P_Reg");

        assertThat(result.bisavoPaternoMae().name()).isEqualTo("BPM_Name");
        assertThat(result.bisavoPaternoMae().registrationNumber()).isEqualTo("BPM_Reg");

        assertThat(result.bisavoPaternaMae().name()).isEqualTo("BPA_M_Name");
        assertThat(result.bisavoPaternaMae().registrationNumber()).isEqualTo("BPA_M_Reg");

        assertThat(result.bisavoMaternoPai().name()).isEqualTo("BMP_Name");
        assertThat(result.bisavoMaternoPai().registrationNumber()).isEqualTo("BMP_Reg");

        assertThat(result.bisavoMaternaPai().name()).isEqualTo("BMA_P_Name");
        assertThat(result.bisavoMaternaPai().registrationNumber()).isEqualTo("BMA_P_Reg");

        assertThat(result.bisavoMaternoMae().name()).isEqualTo("BMM_Name");
        assertThat(result.bisavoMaternoMae().registrationNumber()).isEqualTo("BMM_Reg");

        assertThat(result.bisavoMaternaMae().name()).isEqualTo("BMA_M_Name");
        assertThat(result.bisavoMaternaMae().registrationNumber()).isEqualTo("BMA_M_Reg");
    }

    @Test
    void rootRegistration_isQueriedOnlyOncePerComplementRequest() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyAbccSnapshotVO abcc = new GenealogyAbccSnapshotVO();
        abcc.setAnimalRegistrationNumber("RG-42");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(abcc));

        service.complementWithAbcc(root);

        verify(genealogyAbccQueryPort, times(1)).findGenealogyByRegistrationNumber("RG-42");
    }

    @Test
    void repeatedDeclaredRegistration_isQueriedOnlyOncePerComplementRequest() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "RG-PAI", "Pai Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DEC-SAME-001"), null
        );
        GoatGenealogySnapshot mother = new GoatGenealogySnapshot(
                GoatId.of(20L), "RG-MAE", "Mãe Local", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DEC-SAME-001"), null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), GoatGenealogySnapshot.ParentReference.local(mother)
        );

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-PAI")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-MAE")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DEC-SAME-001")).thenReturn(Optional.empty());

        service.complementWithAbcc(root);

        verify(genealogyAbccQueryPort, times(1)).findGenealogyByRegistrationNumber("DEC-SAME-001");
    }

    @Test
    void rootLookupResult_usedForBothTreeAndIntegration() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setFatherName("ALPHA FATHER");
        rootAbcc.setFatherRegistrationNumber("RG-ALPHA");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().name()).isEqualTo("ALPHA FATHER");
        assertThat(result.pai().registrationNumber()).isEqualTo("RG-ALPHA");
        assertThat(result.integration().status()).isEqualTo("FOUND");
        verify(genealogyAbccQueryPort, times(1)).findGenealogyByRegistrationNumber("RG-42");
    }

    @Test
    void localFather_conflictingRootAbccBranch_doesNotFillPaternalAncestors() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "LOCAL-FATHER-001", "Pai Local", null, null, null, null, null, null, null, null, null, null, null, null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setFatherRegistrationNumber("OTHER-FATHER-999");
        rootAbcc.setPaternalGrandfatherName("OTHER GRANDPA");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("OTHER-GP-999");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("LOCAL-FATHER-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.pai().registrationNumber()).isEqualTo("LOCAL-FATHER-001");

        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
        assertThat(result.avoPaterno().name()).isNull();
        assertThat(result.avoPaterno().registrationNumber()).isNull();
    }

    @Test
    void localMother_conflictingRootAbccBranch_doesNotFillMaternalAncestors() {
        GoatGenealogySnapshot mother = new GoatGenealogySnapshot(
                GoatId.of(20L), "LOCAL-MOTHER-001", "Mãe Local", null, null, null, null, null, null, null, null, null, null, null, null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.local(mother)
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setMotherRegistrationNumber("OTHER-MOTHER-999");
        rootAbcc.setMaternalGrandfatherName("OTHER GRANDPA");
        rootAbcc.setMaternalGrandfatherRegistrationNumber("OTHER-MAT-GP-999");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("LOCAL-MOTHER-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.mae().registrationNumber()).isEqualTo("LOCAL-MOTHER-001");

        assertThat(result.avoMaterno().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
        assertThat(result.avoMaterno().name()).isNull();
        assertThat(result.avoMaterno().registrationNumber()).isNull();
    }

    @Test
    void localFather_matchingRootAbccBranch_canFillPaternalAncestors() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "MATCH-FATHER-001", "Pai Local", null, null, null, null, null, null, null, null, null, null, null, null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setFatherRegistrationNumber("MATCH-FATHER-001");
        rootAbcc.setPaternalGrandfatherName("MATCHING GRANDPA");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("MATCH-GP-001");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("MATCH-FATHER-001")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("MATCH-GP-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.pai().registrationNumber()).isEqualTo("MATCH-FATHER-001");

        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.avoPaterno().name()).isEqualTo("MATCHING GRANDPA");
        assertThat(result.avoPaterno().registrationNumber()).isEqualTo("MATCH-GP-001");
    }

    @Test
    void localMother_matchingRootAbccBranch_canFillMaternalAncestors() {
        GoatGenealogySnapshot mother = new GoatGenealogySnapshot(
                GoatId.of(20L), "MATCH-MOTHER-001", "Mãe Local", null, null, null, null, null, null, null, null, null, null, null, null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.local(mother)
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setMotherRegistrationNumber("MATCH-MOTHER-001");
        rootAbcc.setMaternalGrandmotherName("MATCHING GRANDMA");
        rootAbcc.setMaternalGrandmotherRegistrationNumber("MATCH-GM-001");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("MATCH-MOTHER-001")).thenReturn(Optional.empty());
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("MATCH-GM-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.mae().registrationNumber()).isEqualTo("MATCH-MOTHER-001");

        assertThat(result.avoMaterna().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.avoMaterna().name()).isEqualTo("MATCHING GRANDMA");
        assertThat(result.avoMaterna().registrationNumber()).isEqualTo("MATCH-GM-001");
    }

    @Test
    void abccRuntimeFailure_reportsUnavailable() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenThrow(new RuntimeException("ABCC connection error"));

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.integration().status()).isEqualTo("UNAVAILABLE");
        assertThat(result.integration().message()).contains("Não foi possível consultar a ABCC");
    }

    @Test
    void abccEmptyResult_reportsNotFound() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null, null, null
        );

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.integration().status()).isEqualTo("NOT_FOUND");
        assertThat(result.integration().message()).contains("Não foi possível localizar genealogia complementar");
    }

    @Test
    void localFather_rootAbccParentRegistrationMissing_doesNotUseRootPaternalBranch() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "LOCAL-FATHER-001", "Pai Local", null, null, null, null, null, null, null, null, null, null, null, null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setFatherRegistrationNumber(null);
        rootAbcc.setPaternalGrandfatherName("GHOST GRANDPA");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("GHOST-GP-001");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("LOCAL-FATHER-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
    }

    @Test
    void localMother_rootAbccParentRegistrationMissing_doesNotUseRootMaternalBranch() {
        GoatGenealogySnapshot mother = new GoatGenealogySnapshot(
                GoatId.of(20L), "LOCAL-MOTHER-001", "Mãe Local", null, null, null, null, null, null, null, null, null, null, null, null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.local(mother)
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setMotherRegistrationNumber(null);
        rootAbcc.setMaternalGrandmotherName("GHOST GRANDMA");
        rootAbcc.setMaternalGrandmotherRegistrationNumber("GHOST-GM-001");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("LOCAL-MOTHER-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.avoMaterna().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
    }

    @Test
    void declaredFather_rootAbccParentRegistrationMissing_doesNotUseRootPaternalBranch() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("DEC-FATHER-001"), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setFatherRegistrationNumber(null);
        rootAbcc.setPaternalGrandfatherName("GHOST GRANDPA");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("GHOST-GP-001");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DEC-FATHER-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
    }

    @Test
    void declaredMother_rootAbccParentRegistrationMissing_doesNotUseRootMaternalBranch() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "RG-42", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.external("DEC-MOTHER-001")
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("RG-42");
        rootAbcc.setMotherRegistrationNumber(null);
        rootAbcc.setMaternalGrandmotherName("GHOST GRANDMA");
        rootAbcc.setMaternalGrandmotherRegistrationNumber("GHOST-GM-001");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("RG-42")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("DEC-MOTHER-001")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoMaterna().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
    }

    @Test
    void localFather_slashFormattedRootAbccParent_unlocksPaternalAncestry() {
        GoatGenealogySnapshot father = new GoatGenealogySnapshot(
                GoatId.of(10L), "1635717065", "Pai Local", null, null, null, null, null, null, null, null, null, null, null, null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "1643218012", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.local(father), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("1643218012");
        rootAbcc.setFatherRegistrationNumber("16357/17065");
        rootAbcc.setFatherName("C.V.C SIGNOS PETROLEO");
        rootAbcc.setPaternalGrandfatherName("AVÔ PATERNO ABCC");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("15000/10000");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1635717065")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.pai().registrationNumber()).isEqualTo("1635717065");
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.avoPaterno().name()).isEqualTo("AVÔ PATERNO ABCC");
        assertThat(result.avoPaterno().registrationNumber()).isEqualTo("15000/10000");
    }

    @Test
    void localMother_hyphenFormattedRootAbccParent_unlocksMaternalAncestry() {
        GoatGenealogySnapshot mother = new GoatGenealogySnapshot(
                GoatId.of(20L), "2114517012", "Mãe Local", null, null, null, null, null, null, null, null, null, null, null, null
        );
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "1643218012", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.local(mother)
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("1643218012");
        rootAbcc.setMotherRegistrationNumber("21145-17012");
        rootAbcc.setMotherName("NAIDE");
        rootAbcc.setMaternalGrandmotherName("AVÓ MATERNA ABCC");
        rootAbcc.setMaternalGrandmotherRegistrationNumber("20000-10000");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("2114517012")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.LOCAL);
        assertThat(result.mae().registrationNumber()).isEqualTo("2114517012");
        assertThat(result.avoMaterna().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.avoMaterna().name()).isEqualTo("AVÓ MATERNA ABCC");
    }

    @Test
    void declaredFather_slashFormattedRegistration_unlocksPaternalAncestry() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "1643218012", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("16357/17065"), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("1643218012");
        rootAbcc.setFatherRegistrationNumber("1635717065");
        rootAbcc.setFatherName("C.V.C SIGNOS PETROLEO");
        rootAbcc.setPaternalGrandfatherName("AVÔ PATERNO ABCC");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("1500010000");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1635717065")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.pai().name()).isEqualTo("C.V.C SIGNOS PETROLEO");
        assertThat(result.pai().registrationNumber()).isEqualTo("16357/17065");
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.avoPaterno().name()).isEqualTo("AVÔ PATERNO ABCC");
    }

    @Test
    void declaredMother_dotFormattedRegistration_unlocksMaternalAncestry() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "1643218012", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                null, GoatGenealogySnapshot.ParentReference.external("21145.17012")
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("1643218012");
        rootAbcc.setMotherRegistrationNumber("2114517012");
        rootAbcc.setMotherName("NAIDE");
        rootAbcc.setMaternalGrandfatherName("AVÔ MATERNO ABCC");
        rootAbcc.setMaternalGrandfatherRegistrationNumber("3000010000");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("2114517012")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.mae().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.mae().name()).isEqualTo("NAIDE");
        assertThat(result.mae().registrationNumber()).isEqualTo("21145.17012");
        assertThat(result.avoMaterno().source()).isEqualTo(GenealogyNodeSource.ABCC);
        assertThat(result.avoMaterno().name()).isEqualTo("AVÔ MATERNO ABCC");
    }

    @Test
    void declaredFather_whitespaceAndCaseRegistration_unlocksPaternalAncestry() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "1643218012", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("  ab123-cd456  "), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("1643218012");
        rootAbcc.setFatherRegistrationNumber("AB123CD456");
        rootAbcc.setFatherName("C.V.C SIGNOS PETROLEO");
        rootAbcc.setPaternalGrandfatherName("AVÔ PATERNO ABCC");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("ab123-cd456")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.pai().name()).isEqualTo("C.V.C SIGNOS PETROLEO");
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.ABCC);
    }

    @Test
    void declaredFather_blankRootAbccParentRegistration_doesNotUnlockPaternalAncestry() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "1643218012", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("1635717065"), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("1643218012");
        rootAbcc.setFatherRegistrationNumber("   ");
        rootAbcc.setPaternalGrandfatherName("GHOST GRANDPA");
        rootAbcc.setPaternalGrandfatherRegistrationNumber("1500010000");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1635717065")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
    }

    @Test
    void conflictingParentRegistration_remainsBlocked() {
        GoatGenealogySnapshot root = new GoatGenealogySnapshot(
                GoatId.of(42L), "1643218012", "Cabra Teste", null, null, null, null, null, null, null, null, null, null,
                GoatGenealogySnapshot.ParentReference.external("9999999999"), null
        );

        GenealogyAbccSnapshotVO rootAbcc = new GenealogyAbccSnapshotVO();
        rootAbcc.setAnimalRegistrationNumber("1643218012");
        rootAbcc.setFatherRegistrationNumber("1635717065");
        rootAbcc.setFatherName("C.V.C SIGNOS PETROLEO");
        rootAbcc.setPaternalGrandfatherName("AVÔ PATERNO ABCC");

        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("1643218012")).thenReturn(Optional.of(rootAbcc));
        when(genealogyAbccQueryPort.findGenealogyByRegistrationNumber("9999999999")).thenReturn(Optional.empty());

        GenealogyTreeSnapshot result = service.complementWithAbcc(root);

        assertThat(result.pai().source()).isEqualTo(GenealogyNodeSource.DECLARADO);
        assertThat(result.pai().name()).isNull();
        assertThat(result.avoPaterno().source()).isEqualTo(GenealogyNodeSource.AUSENTE);
    }
}
