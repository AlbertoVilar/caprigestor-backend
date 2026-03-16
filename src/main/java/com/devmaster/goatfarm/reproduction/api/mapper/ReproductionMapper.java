package com.devmaster.goatfarm.reproduction.api.mapper;

import com.devmaster.goatfarm.reproduction.api.dto.*;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReproductionMapper {

    // DTO -> VO (Requests)
    BreedingRequestVO toBreedingRequestVO(BreedingRequestDTO dto);
    PregnancyCheckRequestVO toPregnancyCheckRequestVO(PregnancyCheckRequestDTO dto);
    PregnancyConfirmRequestVO toPregnancyConfirmRequestVO(PregnancyConfirmRequestDTO dto);
    PregnancyCloseRequestVO toPregnancyCloseRequestVO(PregnancyCloseRequestDTO dto);
    CoverageCorrectionRequestVO toCoverageCorrectionRequestVO(CoverageCorrectionRequestDTO dto);
    BirthRequestVO toBirthRequestVO(BirthRequestDTO dto);
    WeaningRequestVO toWeaningRequestVO(WeaningRequestDTO dto);

    // VO -> DTO (Responses)
    PregnancyResponseDTO toPregnancyResponseDTO(PregnancyResponseVO vo);
    ReproductiveEventResponseDTO toReproductiveEventResponseDTO(ReproductiveEventResponseVO vo);
    BirthResponseDTO toBirthResponseDTO(BirthResponseVO vo);
    BirthKidResponseDTO toBirthKidResponseDTO(BirthKidResponseVO vo);
    WeaningResponseDTO toWeaningResponseDTO(WeaningResponseVO vo);

    DiagnosisRecommendationResponseDTO toDiagnosisRecommendationResponseDTO(DiagnosisRecommendationResponseVO vo);
    DiagnosisRecommendationCoverageDTO toDiagnosisRecommendationCoverageDTO(DiagnosisRecommendationCoverageVO vo);
    DiagnosisRecommendationCheckDTO toDiagnosisRecommendationCheckDTO(DiagnosisRecommendationCheckVO vo);
    PregnancyDiagnosisAlertItemDTO toPregnancyDiagnosisAlertItemDTO(PregnancyDiagnosisAlertVO vo);

}
