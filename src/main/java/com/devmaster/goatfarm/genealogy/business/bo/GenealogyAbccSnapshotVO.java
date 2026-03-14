package com.devmaster.goatfarm.genealogy.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenealogyAbccSnapshotVO {

    private String externalId;

    private String animalName;
    private String animalRegistrationNumber;

    private String fatherName;
    private String fatherRegistrationNumber;
    private String motherName;
    private String motherRegistrationNumber;

    private String paternalGrandfatherName;
    private String paternalGrandfatherRegistrationNumber;
    private String paternalGrandmotherName;
    private String paternalGrandmotherRegistrationNumber;
    private String maternalGrandfatherName;
    private String maternalGrandfatherRegistrationNumber;
    private String maternalGrandmotherName;
    private String maternalGrandmotherRegistrationNumber;

    private String bisavoPaternoPaiName;
    private String bisavoPaternoPaiRegistrationNumber;
    private String bisavoPaternaPaiName;
    private String bisavoPaternaPaiRegistrationNumber;
    private String bisavoPaternoMaeName;
    private String bisavoPaternoMaeRegistrationNumber;
    private String bisavoPaternaMaeName;
    private String bisavoPaternaMaeRegistrationNumber;
    private String bisavoMaternoPaiName;
    private String bisavoMaternoPaiRegistrationNumber;
    private String bisavoMaternaPaiName;
    private String bisavoMaternaPaiRegistrationNumber;
    private String bisavoMaternoMaeName;
    private String bisavoMaternoMaeRegistrationNumber;
    private String bisavoMaternaMaeName;
    private String bisavoMaternaMaeRegistrationNumber;
}

