package com.devmaster.goatfarm.goat.api.mapper;

import com.devmaster.goatfarm.goat.api.dto.GoatAbccPreviewRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccPreviewResponseDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccSearchItemDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccSearchRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccSearchResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchResponseVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoatAbccImportMapper {

    public GoatAbccSearchRequestVO toSearchRequestVO(GoatAbccSearchRequestDTO dto) {
        Integer page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        return GoatAbccSearchRequestVO.builder()
                .raceId(dto.getRaceId())
                .affix(dto.getAffix())
                .page(page)
                .sex(dto.getSex())
                .tod(dto.getTod())
                .toe(dto.getToe())
                .name(dto.getName())
                .dna(dto.getDna())
                .build();
    }

    public GoatAbccPreviewRequestVO toPreviewRequestVO(GoatAbccPreviewRequestDTO dto) {
        return GoatAbccPreviewRequestVO.builder()
                .externalId(dto.getExternalId())
                .build();
    }

    public GoatAbccSearchResponseDTO toSearchResponseDTO(GoatAbccSearchResponseVO vo) {
        List<GoatAbccSearchItemDTO> items = vo.getItems() == null ? List.of() : vo.getItems().stream()
                .map(this::toSearchItemDTO)
                .toList();
        return GoatAbccSearchResponseDTO.builder()
                .currentPage(vo.getCurrentPage())
                .totalPages(vo.getTotalPages())
                .pageSize(vo.getPageSize())
                .items(items)
                .build();
    }

    public GoatAbccPreviewResponseDTO toPreviewResponseDTO(GoatAbccPreviewResponseVO vo) {
        return GoatAbccPreviewResponseDTO.builder()
                .externalSource(vo.getExternalSource())
                .externalId(vo.getExternalId())
                .registrationNumber(vo.getRegistrationNumber())
                .name(vo.getName())
                .gender(vo.getGender())
                .breed(vo.getBreed())
                .color(vo.getColor())
                .birthDate(vo.getBirthDate())
                .status(vo.getStatus())
                .tod(vo.getTod())
                .toe(vo.getToe())
                .category(vo.getCategory())
                .fatherName(vo.getFatherName())
                .fatherRegistrationNumber(vo.getFatherRegistrationNumber())
                .motherName(vo.getMotherName())
                .motherRegistrationNumber(vo.getMotherRegistrationNumber())
                .userName(vo.getUserName())
                .farmId(vo.getFarmId())
                .farmName(vo.getFarmName())
                .normalizationWarnings(vo.getNormalizationWarnings())
                .build();
    }

    private GoatAbccSearchItemDTO toSearchItemDTO(GoatAbccSearchItemVO vo) {
        return GoatAbccSearchItemDTO.builder()
                .externalSource(vo.getExternalSource())
                .externalId(vo.getExternalId())
                .nome(vo.getNome())
                .situacao(vo.getSituacao())
                .dna(vo.getDna())
                .tod(vo.getTod())
                .toe(vo.getToe())
                .criador(vo.getCriador())
                .afixo(vo.getAfixo())
                .dataNascimento(vo.getDataNascimento())
                .sexo(vo.getSexo())
                .raca(vo.getRaca())
                .pelagem(vo.getPelagem())
                .normalizedGender(vo.getNormalizedGender())
                .normalizedBreed(vo.getNormalizedBreed())
                .normalizedStatus(vo.getNormalizedStatus())
                .normalizationWarnings(vo.getNormalizationWarnings())
                .build();
    }
}

