package com.devmaster.goatfarm.reproduction.api.mapper;

import com.devmaster.goatfarm.reproduction.api.dto.*;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReproductionMapper {

    // DTO -> VO (Requests)
    BreedingRequestVO toBreedingRequestVO(BreedingRequestDTO dto);
    PregnancyConfirmRequestVO toPregnancyConfirmRequestVO(PregnancyConfirmRequestDTO dto);
    PregnancyCloseRequestVO toPregnancyCloseRequestVO(PregnancyCloseRequestDTO dto);
    CoverageCorrectionRequestVO toCoverageCorrectionRequestVO(CoverageCorrectionRequestDTO dto);

    // Entity -> VO (Responses)
    PregnancyResponseVO toPregnancyResponseVO(Pregnancy entity);
    ReproductiveEventResponseVO toReproductiveEventResponseVO(ReproductiveEvent entity);

    // VO -> DTO (Responses)
    PregnancyResponseDTO toPregnancyResponseDTO(PregnancyResponseVO vo);
    ReproductiveEventResponseDTO toReproductiveEventResponseDTO(ReproductiveEventResponseVO vo);

    DiagnosisRecommendationResponseDTO toDiagnosisRecommendationResponseDTO(DiagnosisRecommendationResponseVO vo);
    DiagnosisRecommendationCoverageDTO toDiagnosisRecommendationCoverageDTO(DiagnosisRecommendationCoverageVO vo);
    DiagnosisRecommendationCheckDTO toDiagnosisRecommendationCheckDTO(DiagnosisRecommendationCheckVO vo);
    
    // VO -> Entity (Optional, usually handled in Business manually or via Mapper)
    // For skeleton, strictly following "RequestDTO→RequestVO, Entity→ResponseVO, ResponseVO→ResponseDTO" as per prompt.
}
