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

    // VO -> DTO (Responses)
    PregnancyResponseDTO toPregnancyResponseDTO(PregnancyResponseVO vo);
    ReproductiveEventResponseDTO toReproductiveEventResponseDTO(ReproductiveEventResponseVO vo);

    DiagnosisRecommendationResponseDTO toDiagnosisRecommendationResponseDTO(DiagnosisRecommendationResponseVO vo);
    DiagnosisRecommendationCoverageDTO toDiagnosisRecommendationCoverageDTO(DiagnosisRecommendationCoverageVO vo);
    DiagnosisRecommendationCheckDTO toDiagnosisRecommendationCheckDTO(DiagnosisRecommendationCheckVO vo);
    
}
