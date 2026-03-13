package com.devmaster.goatfarm.goat.api.mapper;

import com.devmaster.goatfarm.goat.api.dto.GoatAbccBatchConfirmItemDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccBatchConfirmItemResultDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccBatchConfirmRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccBatchConfirmResponseDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccPreviewRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccPreviewResponseDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccRaceOptionDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccRaceOptionsResponseDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccSearchItemDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccSearchRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatAbccSearchResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
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
                .raceName(dto.getRaceName())
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

    public List<GoatAbccBatchConfirmItemVO> toBatchConfirmItemsVO(GoatAbccBatchConfirmRequestDTO dto) {
        if (dto == null || dto.getItems() == null) {
            return List.of();
        }
        return dto.getItems().stream()
                .map(this::toBatchConfirmItemVO)
                .toList();
    }

    public GoatAbccRaceOptionsResponseDTO toRaceOptionsResponseDTO(List<GoatAbccRaceOptionVO> raceOptions) {
        List<GoatAbccRaceOptionDTO> items = raceOptions == null
                ? List.of()
                : raceOptions.stream().map(this::toRaceOptionDTO).toList();

        return GoatAbccRaceOptionsResponseDTO.builder()
                .items(items)
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

    public GoatAbccBatchConfirmResponseDTO toBatchConfirmResponseDTO(GoatAbccBatchConfirmResponseVO vo) {
        List<GoatAbccBatchConfirmItemResultDTO> results = vo.getResults() == null
                ? List.of()
                : vo.getResults().stream().map(this::toBatchConfirmItemResultDTO).toList();

        return GoatAbccBatchConfirmResponseDTO.builder()
                .totalSelected(vo.getTotalSelected())
                .totalImported(vo.getTotalImported())
                .totalSkippedDuplicate(vo.getTotalSkippedDuplicate())
                .totalSkippedTodMismatch(vo.getTotalSkippedTodMismatch())
                .totalError(vo.getTotalError())
                .results(results)
                .build();
    }

    private GoatAbccBatchConfirmItemVO toBatchConfirmItemVO(GoatAbccBatchConfirmItemDTO dto) {
        return GoatAbccBatchConfirmItemVO.builder()
                .externalId(dto != null ? dto.getExternalId() : null)
                .build();
    }

    private GoatAbccRaceOptionDTO toRaceOptionDTO(GoatAbccRaceOptionVO vo) {
        return GoatAbccRaceOptionDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .normalizedBreed(vo.getNormalizedBreed())
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

    private GoatAbccBatchConfirmItemResultDTO toBatchConfirmItemResultDTO(GoatAbccBatchConfirmItemResultVO vo) {
        return GoatAbccBatchConfirmItemResultDTO.builder()
                .externalId(vo.getExternalId())
                .registrationNumber(vo.getRegistrationNumber())
                .name(vo.getName())
                .status(vo.getStatus())
                .message(vo.getMessage())
                .build();
    }
}
