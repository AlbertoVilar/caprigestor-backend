package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ExternalServiceUnavailableException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.business.abcc.AbccAnimalTranslator;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRegistrationLookupRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRegistrationLookupResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchResponseVO;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoatAbccQueryBusiness implements GoatAbccQueryUseCase {

    private static final String MSG_ABCC_UNAVAILABLE = "Não foi possível consultar a ABCC pública no momento.";
    private static final String MSG_PREVIEW_UNAVAILABLE = "Não foi possível obter o preview do animal na ABCC pública.";

    private final GoatAbccPublicQueryPort abccPublicQueryPort;
    private final GoatFarmPersistencePort goatFarmPort;
    private final EntityFinder entityFinder;
    private final AbccAnimalTranslator animalTranslator;

    public GoatAbccQueryBusiness(
            GoatAbccPublicQueryPort abccPublicQueryPort,
            GoatFarmPersistencePort goatFarmPort,
            EntityFinder entityFinder,
            AbccAnimalTranslator animalTranslator
    ) {
        this.abccPublicQueryPort = abccPublicQueryPort;
        this.goatFarmPort = goatFarmPort;
        this.entityFinder = entityFinder;
        this.animalTranslator = animalTranslator;
    }

    @Override
    public List<GoatAbccRaceOptionVO> listRaces(Long farmId) {
        return fetchAbccRaceCatalog().stream()
                .map(animalTranslator::toNormalizedRaceOption)
                .toList();
    }

    @Override
    public GoatAbccSearchResponseVO search(Long farmId, GoatAbccSearchRequestVO requestVO) {
        validateSearchRequest(requestVO);

        Integer resolvedRaceId = resolveRaceId(requestVO);
        GoatAbccSearchRequestVO normalizedRequest = GoatAbccSearchRequestVO.builder()
                .raceId(resolvedRaceId)
                .raceName(requestVO.getRaceName())
                .affix(requestVO.getAffix())
                .page(requestVO.getPage())
                .sex(requestVO.getSex())
                .tod(requestVO.getTod())
                .toe(requestVO.getToe())
                .name(requestVO.getName())
                .dna(requestVO.getDna())
                .build();

        GoatAbccRawSearchResultVO rawResult;
        try {
            rawResult = abccPublicQueryPort.search(normalizedRequest);
        } catch (ExternalServiceUnavailableException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", MSG_ABCC_UNAVAILABLE);
        }

        List<GoatAbccSearchItemVO> normalizedItems = rawResult.getItems() == null
                ? List.of()
                : rawResult.getItems().stream().map(animalTranslator::normalizeSearchItem).toList();

        return GoatAbccSearchResponseVO.builder()
                .currentPage(rawResult.getCurrentPage())
                .totalPages(rawResult.getTotalPages())
                .pageSize(normalizedItems.size())
                .items(normalizedItems)
                .build();
    }

    @Override
    public GoatAbccPreviewResponseVO preview(Long farmId, GoatAbccPreviewRequestVO requestVO) {
        if (requestVO == null || isBlank(requestVO.getExternalId())) {
            throw new BusinessRuleException("externalId", "Identificador externo da ABCC é obrigatório.");
        }

        FarmRecord farm = loadFarm(farmId);

        GoatAbccRawPreviewVO raw;
        try {
            raw = abccPublicQueryPort.preview(requestVO.getExternalId());
        } catch (ExternalServiceUnavailableException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", MSG_PREVIEW_UNAVAILABLE);
        }

        return animalTranslator.toPreview(raw, farmId, farm.name());
    }

    @Override
    public GoatAbccRegistrationLookupResponseVO lookupByRegistration(
            Long farmId,
            GoatAbccRegistrationLookupRequestVO requestVO
    ) {
        if (requestVO == null || requestVO.getRaceId() == null || requestVO.getRaceId() < 1) {
            throw new BusinessRuleException("raceId", "Raça ABCC é obrigatória antes da consulta.");
        }

        String requestedRegistration = animalTranslator.normalizeRegistrationForLookup(requestVO.getRegistrationNumber());
        if (requestedRegistration == null) {
            throw new BusinessRuleException("registrationNumber", "Número de registro é obrigatório.");
        }

        GoatAbccRaceOptionVO selectedRace = fetchAbccRaceCatalog().stream()
                .filter(option -> requestVO.getRaceId().equals(option.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("raceId", "Raça ABCC inválida."));

        GoatAbccRawSearchResultVO rawResult;
        try {
            rawResult = abccPublicQueryPort.searchByRegistration(requestVO.getRaceId(), requestedRegistration);
        } catch (ExternalServiceUnavailableException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", MSG_ABCC_UNAVAILABLE);
        }

        List<GoatAbccSearchItemVO> candidates = rawResult == null || rawResult.getItems() == null
                ? List.of()
                : rawResult.getItems().stream()
                        .filter(item -> matchesRegistrationAndRace(item, requestedRegistration, selectedRace))
                        .map(animalTranslator::normalizeSearchItem)
                        .toList();

        if (candidates.isEmpty()) {
            return GoatAbccRegistrationLookupResponseVO.builder()
                    .status("NOT_FOUND")
                    .message("Animal não localizado na ABCC para a raça e registro informados.")
                    .candidates(List.of())
                    .build();
        }

        if (candidates.size() > 1) {
            return GoatAbccRegistrationLookupResponseVO.builder()
                    .status("AMBIGUOUS")
                    .message("Mais de um animal foi localizado para a mesma raça e registro. Selecione um candidato.")
                    .candidates(candidates)
                    .build();
        }

        GoatAbccSearchItemVO candidate = candidates.getFirst();
        if (isBlank(candidate.getExternalId())) {
            throw new BusinessRuleException("abcc", "A ABCC retornou um candidato sem identificador externo.");
        }

        GoatAbccPreviewResponseVO previewResponse = preview(
                farmId,
                GoatAbccPreviewRequestVO.builder().externalId(candidate.getExternalId()).build()
        );
        validateLookupPreview(previewResponse, requestedRegistration, selectedRace);

        return GoatAbccRegistrationLookupResponseVO.builder()
                .status("FOUND")
                .message("Animal localizado na ABCC. Revise os dados antes de confirmar.")
                .preview(previewResponse)
                .candidates(List.of())
                .build();
    }

    private void validateSearchRequest(GoatAbccSearchRequestVO requestVO) {
        if (requestVO == null) {
            throw new BusinessRuleException("payload", "Payload de busca ABCC é obrigatório.");
        }
        if (requestVO.getRaceId() == null && isBlank(requestVO.getRaceName())) {
            throw new BusinessRuleException("raceName", "Raça ABCC é obrigatória.");
        }
        if (isBlank(requestVO.getAffix())) {
            throw new BusinessRuleException("affix", "Afixo é obrigatório para busca na ABCC.");
        }
        if (requestVO.getPage() != null && requestVO.getPage() < 1) {
            throw new BusinessRuleException("page", "Página deve ser maior ou igual a 1.");
        }
    }

    private Integer resolveRaceId(GoatAbccSearchRequestVO requestVO) {
        if (requestVO.getRaceId() != null && requestVO.getRaceId() > 0) {
            return requestVO.getRaceId();
        }

        String requestedRaceName = trimOrNull(requestVO.getRaceName());
        if (requestedRaceName == null) {
            throw new BusinessRuleException("raceName", "Raça ABCC é obrigatória.");
        }

        List<GoatAbccRaceOptionVO> raceOptions = fetchAbccRaceCatalog();
        String requestedToken = animalTranslator.normalizedToken(requestedRaceName);

        return raceOptions.stream()
                .filter(option -> animalTranslator.normalizedToken(option.getName()).equals(requestedToken))
                .map(GoatAbccRaceOptionVO::getId)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        "raceName",
                        "Raça ABCC inválida. Consulte a lista de raças disponíveis antes de buscar."
                ));
    }

    private List<GoatAbccRaceOptionVO> fetchAbccRaceCatalog() {
        try {
            List<GoatAbccRaceOptionVO> raceOptions = abccPublicQueryPort.listRaces();
            if (raceOptions == null || raceOptions.isEmpty()) {
                throw new BusinessRuleException("abcc", "Não foi possível carregar a lista de raças da ABCC.");
            }
            return raceOptions;
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (ExternalServiceUnavailableException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessRuleException("abcc", "Não foi possível carregar a lista de raças da ABCC pública.");
        }
    }

    private boolean matchesRegistrationAndRace(
            GoatAbccRawSearchItemVO item,
            String requestedRegistration,
            GoatAbccRaceOptionVO selectedRace
    ) {
        String returnedRegistration = item == null
                ? null
                : animalTranslator.normalizeRegistrationForLookup(valueOrEmpty(item.getTod()) + valueOrEmpty(item.getToe()));
        if (item == null || !requestedRegistration.equals(returnedRegistration)) {
            return false;
        }
        String returnedRace = animalTranslator.normalizedToken(item.getRaca());
        String selectedRaceName = animalTranslator.normalizedToken(selectedRace.getName());
        GoatBreed selectedBreed = animalTranslator.normalizeBreedInternal(selectedRace.getName());
        GoatBreed returnedBreed = animalTranslator.normalizeBreedInternal(item.getRaca());
        return returnedRace.equals(selectedRaceName)
                || (selectedBreed != null && selectedBreed == returnedBreed);
    }

    private void validateLookupPreview(
            GoatAbccPreviewResponseVO previewResponse,
            String requestedRegistration,
            GoatAbccRaceOptionVO selectedRace
    ) {
        String previewRegistration = animalTranslator.normalizeRegistrationForLookup(previewResponse.getRegistrationNumber());
        if (!requestedRegistration.equals(previewRegistration)) {
            throw new BusinessRuleException("abcc", "A ABCC retornou registro divergente do solicitado.");
        }

        GoatBreed selectedBreed = animalTranslator.normalizeBreedInternal(selectedRace.getName());
        GoatBreed previewBreed = previewResponse.getBreed();
        String selectedRaceName = animalTranslator.normalizedToken(selectedRace.getName());
        if (previewBreed == null
                || (selectedBreed != null && previewBreed != selectedBreed)
                || (selectedBreed == null && !selectedRaceName.equals(animalTranslator.normalizedToken(previewResponse.getBreed().name())))) {
            throw new BusinessRuleException("abcc", "A ABCC retornou raça divergente da selecionada.");
        }
    }

    private FarmRecord loadFarm(Long farmId) {
        return entityFinder.findOrThrow(() -> goatFarmPort.findById(farmId), "Fazenda não encontrada.");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
