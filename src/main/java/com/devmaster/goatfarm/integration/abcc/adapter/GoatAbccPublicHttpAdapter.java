package com.devmaster.goatfarm.integration.abcc.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.devmaster.goatfarm.genealogy.application.ports.out.GenealogyAbccQueryPort;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatExternalParentQueryPort;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.enums.Gender;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Adapter for the current JavaScript/JSON public SisCapri API. */
@Component
public class GoatAbccPublicHttpAdapter implements GoatAbccPublicQueryPort,
        GoatExternalParentQueryPort, GenealogyAbccQueryPort {

    private static final String BASE_URL = "https://siscapri.abccaprinos.com.br";
    private static final String API_URL = BASE_URL + "/x.php?m=siscapri.api.api_genealogia&site=siscapri";
    private static final String OP_RACES = "getRacas";
    private static final String OP_SEARCH = "getAnimaisPublico";
    private static final String OP_DETAILS = "getDetalhesAnimalCompleto";
    /** The legacy public search returned ten rows per page; keep that boundary locally. */
    private static final int SEARCH_PAGE_SIZE = 10;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiUrl;
    private long connectTimeoutSeconds = 30;
    private long requestTimeoutSeconds = 60;
    private int maxAttempts = 2;
    private long retryBackoffMillis = 150;
    private int maxResponseBytes = 2 * 1024 * 1024;

    public GoatAbccPublicHttpAdapter() {
        this(API_URL);
    }

    GoatAbccPublicHttpAdapter(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @org.springframework.beans.factory.annotation.Value("${caprigestor.abcc.connect-timeout-seconds:30}")
    void setConnectTimeoutSeconds(long value) { connectTimeoutSeconds = value; }

    @org.springframework.beans.factory.annotation.Value("${caprigestor.abcc.request-timeout-seconds:60}")
    void setRequestTimeoutSeconds(long value) { requestTimeoutSeconds = value; }

    @org.springframework.beans.factory.annotation.Value("${caprigestor.abcc.max-attempts:2}")
    void setMaxAttempts(int value) { maxAttempts = value; }

    @org.springframework.beans.factory.annotation.Value("${caprigestor.abcc.retry-backoff-millis:150}")
    void setRetryBackoffMillis(long value) { retryBackoffMillis = value; }

    @org.springframework.beans.factory.annotation.Value("${caprigestor.abcc.max-response-bytes:2097152}")
    void setMaxResponseBytes(int value) { maxResponseBytes = value; }

    @Override
    public List<GoatAbccRaceOptionVO> listRaces() {
        try {
            JsonNode response = postJson(Map.of("runat", "client", "f", OP_RACES));
            if (!response.isArray()) throw malformed("ABCC retornou lista de raças com formato inválido.");
            List<GoatAbccRaceOptionVO> result = new ArrayList<>();
            for (JsonNode race : response) {
                Integer id = integer(race, "rc_id");
                String name = text(race, "rc_descricao");
                if (id != null && id > 0 && !isBlank(name)) {
                    result.add(GoatAbccRaceOptionVO.builder().id(id).name(name).build());
                }
            }
            return result;
        } catch (AbccIntegrationException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new AbccUnavailableException("Falha de comunicação com a ABCC pública.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AbccUnavailableException("Consulta à ABCC foi interrompida.", ex);
        } catch (Exception ex) {
            throw malformed("Resposta inválida da ABCC pública.", ex);
        }
    }

    @Override
    public GoatAbccRawSearchResultVO search(GoatAbccSearchRequestVO request) {
        try {
            Map<String, String> form = new LinkedHashMap<>();
            form.put("runat", "client");
            form.put("f", OP_SEARCH);
            form.put("raca", value(request == null ? null : request.getRaceId()));
            form.put("termo", value(request == null ? null : request.getName()));
            form.put("afixo", value(request == null ? null : request.getAffix()));
            form.put("sexo", value(request == null ? null : request.getSex()));
            form.put("dna", value(request == null ? null : request.getDna()));
            form.put("tod", value(request == null ? null : request.getTod()));
            form.put("toe", value(request == null ? null : request.getToe()));
            return mapSearchResult(postJson(form), request == null ? null : request.getPage());
        } catch (AbccIntegrationException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new AbccUnavailableException("Falha de comunicação com a ABCC pública.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AbccUnavailableException("Consulta à ABCC foi interrompida.", ex);
        } catch (Exception ex) {
            throw malformed("Resposta inválida da ABCC pública.", ex);
        }
    }

    @Override
    public GoatAbccRawSearchResultVO searchByRegistration(Integer raceId, String registrationNumber) {
        if (isBlank(registrationNumber)) return emptySearch();
        try {
            Map<String, String> form = searchForm(raceId, registrationNumber);
            JsonNode response = postJson(form);
            String expected = normalizeRegistration(registrationNumber);
            List<GoatAbccRawSearchItemVO> matches = new ArrayList<>();
            if (!response.isArray()) throw malformed("ABCC retornou pesquisa com formato inválido.");
            for (JsonNode item : response) {
                if (isObjectWithIdentity(item)
                        && expected.equals(normalizeRegistration(text(item, "registro")))) {
                    matches.add(mapSearchItem(item));
                }
            }
            return GoatAbccRawSearchResultVO.builder().currentPage(1).totalPages(1)
                    .offset(0).items(matches).build();
        } catch (AbccIntegrationException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new AbccUnavailableException("Falha de comunicação com a ABCC pública.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AbccUnavailableException("Consulta à ABCC foi interrompida.", ex);
        } catch (Exception ex) {
            throw malformed("Resposta inválida da ABCC pública.", ex);
        }
    }

    @Override
    public GoatAbccRawPreviewVO preview(String externalId) {
        try {
            return mapPreview(externalId, loadDetails(externalId));
        } catch (AbccIntegrationException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new AbccUnavailableException("Falha de comunicação com a ABCC pública.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AbccUnavailableException("Consulta à ABCC foi interrompida.", ex);
        } catch (Exception ex) {
            throw malformed("Resposta inválida da ABCC pública.", ex);
        }
    }

    @Override
    public Optional<GenealogyAbccSnapshotVO> findGenealogyByRegistrationNumber(String registrationNumber) {
        if (isBlank(registrationNumber)) return Optional.empty();
        try {
            String expected = normalizeRegistration(registrationNumber);
            JsonNode exact = findExactRegistration(postJson(searchForm(null, registrationNumber)), expected);
            if (exact == null) return Optional.empty();
            String externalId = text(exact, "id");
            if (isBlank(externalId)) throw malformed("ABCC retornou animal sem identificador externo.");
            GenealogyAbccSnapshotVO snapshot = mapGenealogy(externalId, loadDetails(externalId));
            if (snapshot == null || !expected.equals(normalizeRegistration(snapshot.getAnimalRegistrationNumber()))) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        } catch (AbccIntegrationException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new AbccUnavailableException("Falha de comunicação com a ABCC pública.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AbccUnavailableException("Consulta à ABCC foi interrompida.", ex);
        } catch (Exception ex) {
            throw malformed("Resposta inválida da ABCC pública.", ex);
        }
    }

    @Override
    public Optional<ExternalParentReference> findByRegistrationNumber(String registrationNumber) {
        return findGenealogyByRegistrationNumber(registrationNumber)
                .map(snapshot -> new ExternalParentReference(
                        snapshot.getAnimalRegistrationNumber(), snapshot.getAnimalGender()));
    }

    private JsonNode loadDetails(String externalId) throws IOException, InterruptedException {
        if (isBlank(externalId)) throw malformed("Identificador externo ABCC ausente.");
        return postJson(Map.of("runat", "client", "f", OP_DETAILS, "id", externalId.trim()));
    }

    private JsonNode postJson(Map<String, String> form) throws IOException, InterruptedException {
        HttpClient client = newClient();
        String body = new AbccHttpTransport(client, Duration.ofSeconds(Math.max(1, requestTimeoutSeconds)),
                maxAttempts, retryBackoffMillis, maxResponseBytes).post(apiUrl, formEncode(form));
        try {
            JsonNode json = objectMapper.readTree(body);
            if (json == null || json.isNull()) throw malformed("ABCC retornou JSON vazio.");
            return json;
        } catch (AbccIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw malformed("ABCC retornou JSON inválido.", ex);
        }
    }

    private Map<String, String> searchForm(Integer raceId, String registration) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("runat", "client");
        form.put("f", OP_SEARCH);
        // Empty race is accepted by the public API for an exact registration lookup.
        form.put("raca", value(raceId));
        form.put("termo", registration.trim());
        form.put("afixo", "");
        form.put("sexo", "");
        form.put("dna", "");
        form.put("tod", "");
        form.put("toe", "");
        return form;
    }

    private GoatAbccRawSearchResultVO mapSearchResult(JsonNode response, Integer requestedPage) {
        if (!response.isArray()) throw malformed("ABCC retornou pesquisa com formato inválido.");
        List<GoatAbccRawSearchItemVO> allItems = new ArrayList<>();
        for (JsonNode item : response) {
            if (isObjectWithIdentity(item)) allItems.add(mapSearchItem(item));
        }
        int totalPages = Math.max(1, (int) Math.ceil(allItems.size() / (double) SEARCH_PAGE_SIZE));
        int currentPage = Math.max(1, Math.min(requestedPage == null ? 1 : requestedPage, totalPages));
        int fromIndex = Math.min((currentPage - 1) * SEARCH_PAGE_SIZE, allItems.size());
        int toIndex = Math.min(fromIndex + SEARCH_PAGE_SIZE, allItems.size());
        List<GoatAbccRawSearchItemVO> pageItems = allItems.subList(fromIndex, toIndex);
        return GoatAbccRawSearchResultVO.builder().currentPage(currentPage).totalPages(totalPages)
                .offset(fromIndex).currentUrl(null).items(pageItems).build();
    }

    private GoatAbccRawSearchItemVO mapSearchItem(JsonNode item) {
        String registration = text(item, "registro");
        String normalizedRegistration = normalizeRegistration(registration);
        String tod = text(item, "tod");
        String toe = text(item, "toe");
        if ((isBlank(tod) || isBlank(toe)) && normalizedRegistration.length() >= 10) {
            tod = normalizedRegistration.substring(0, 5);
            toe = normalizedRegistration.substring(5);
        }
        return GoatAbccRawSearchItemVO.builder().externalId(text(item, "id"))
                .nome(text(item, "nome")).situacao(text(item, "situacao"))
                .dna(text(item, "dna")).tod(tod).toe(toe)
                .criador(text(item, "criador")).afixo(text(item, "afixo"))
                .dataNascimento(firstNonBlank(text(item, "dataNascimento"), text(item, "data_nasce")))
                .sexo(text(item, "sexo")).raca(text(item, "raca"))
                .pelagem(text(item, "pelagem")).build();
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private GoatAbccRawPreviewVO mapPreview(String externalId, JsonNode details) {
        JsonNode data = requiredObject(details, "dados_cadastrais");
        JsonNode parents = details.path("pais");
        return GoatAbccRawPreviewVO.builder().externalId(externalId)
                .nome(text(data, "nome")).registro(text(data, "registro"))
                .criador(text(data, "criador")).proprietario(text(data, "proprietario"))
                .raca(text(data, "raca")).pelagem(text(data, "pelagem"))
                .situacao(text(data, "situacao")).sexo(text(data, "sexo"))
                .categoria(text(data, "categoria")).tod(text(data, "tod"))
                .toe(text(data, "toe")).dataNascimento(text(data, "data_nasce"))
                .paiNome(text(parents.path("pai"), "nome"))
                .paiRegistro(text(parents.path("pai"), "registro"))
                .maeNome(text(parents.path("mae"), "nome"))
                .maeRegistro(text(parents.path("mae"), "registro")).build();
    }

    private GenealogyAbccSnapshotVO mapGenealogy(String externalId, JsonNode details) {
        JsonNode data = requiredObject(details, "dados_cadastrais");
        JsonNode tree = requiredObject(details, "arvore");
        JsonNode animal = requiredObject(tree, "animal");
        JsonNode paternal = tree.path("avos_paternos");
        JsonNode maternal = tree.path("avos_maternos");
        JsonNode great = tree.path("bisavos");
        return GenealogyAbccSnapshotVO.builder().externalId(externalId)
                .animalName(text(animal, "nome")).animalRegistrationNumber(text(animal, "registro"))
                .animalGender(parseGender(text(data, "sexo")))
                .fatherName(text(tree.path("pai"), "nome"))
                .fatherRegistrationNumber(text(tree.path("pai"), "registro"))
                .motherName(text(tree.path("mae"), "nome"))
                .motherRegistrationNumber(text(tree.path("mae"), "registro"))
                .paternalGrandfatherName(text(paternal.path("pai"), "nome"))
                .paternalGrandfatherRegistrationNumber(text(paternal.path("pai"), "registro"))
                .paternalGrandmotherName(text(paternal.path("mae"), "nome"))
                .paternalGrandmotherRegistrationNumber(text(paternal.path("mae"), "registro"))
                .maternalGrandfatherName(text(maternal.path("pai"), "nome"))
                .maternalGrandfatherRegistrationNumber(text(maternal.path("pai"), "registro"))
                .maternalGrandmotherName(text(maternal.path("mae"), "nome"))
                .maternalGrandmotherRegistrationNumber(text(maternal.path("mae"), "registro"))
                .bisavoPaternoPaiName(text(great.path("paterno_pai_pai"), "nome"))
                .bisavoPaternoPaiRegistrationNumber(text(great.path("paterno_pai_pai"), "registro"))
                .bisavoPaternoMaeName(text(great.path("paterno_pai_mae"), "nome"))
                .bisavoPaternoMaeRegistrationNumber(text(great.path("paterno_pai_mae"), "registro"))
                .bisavoPaternaPaiName(text(great.path("paterno_mae_pai"), "nome"))
                .bisavoPaternaPaiRegistrationNumber(text(great.path("paterno_mae_pai"), "registro"))
                .bisavoPaternaMaeName(text(great.path("paterno_mae_mae"), "nome"))
                .bisavoPaternaMaeRegistrationNumber(text(great.path("paterno_mae_mae"), "registro"))
                .bisavoMaternoPaiName(text(great.path("materno_pai_pai"), "nome"))
                .bisavoMaternoPaiRegistrationNumber(text(great.path("materno_pai_pai"), "registro"))
                .bisavoMaternoMaeName(text(great.path("materno_pai_mae"), "nome"))
                .bisavoMaternoMaeRegistrationNumber(text(great.path("materno_pai_mae"), "registro"))
                .bisavoMaternaPaiName(text(great.path("materno_mae_pai"), "nome"))
                .bisavoMaternaPaiRegistrationNumber(text(great.path("materno_mae_pai"), "registro"))
                .bisavoMaternaMaeName(text(great.path("materno_mae_mae"), "nome"))
                .bisavoMaternaMaeRegistrationNumber(text(great.path("materno_mae_mae"), "registro"))
                .build();
    }

    private JsonNode findExactRegistration(JsonNode response, String expected) {
        if (!response.isArray()) throw malformed("ABCC retornou pesquisa com formato inválido.");
        for (JsonNode item : response) {
            if (isObjectWithIdentity(item)
                    && expected.equals(normalizeRegistration(text(item, "registro")))) return item;
        }
        return null;
    }

    private boolean isObjectWithIdentity(JsonNode item) {
        return item != null && item.isObject() && !isBlank(text(item, "id"))
                && !isBlank(text(item, "registro"));
    }

    private JsonNode requiredObject(JsonNode parent, String field) {
        JsonNode value = parent.path(field);
        if (!value.isObject()) throw malformed("ABCC não retornou o campo obrigatório " + field + ".");
        return value;
    }

    private Integer integer(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.canConvertToInt() ? value.intValue() : null;
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !value.isValueNode()) return null;
        String result = value.asText();
        return isBlank(result) ? null : result.trim();
    }

    private String value(Object value) { return value == null ? "" : String.valueOf(value); }

    private GoatAbccRawSearchResultVO emptySearch() {
        return GoatAbccRawSearchResultVO.builder().currentPage(1).totalPages(1)
                .offset(0).items(List.of()).build();
    }

    private HttpClient newClient() {
        CookieManager cookies = new CookieManager();
        cookies.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        return HttpClient.newBuilder().cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(Math.max(1, connectTimeoutSeconds))).build();
    }

    private String formEncode(Map<String, String> form) {
        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (encoded.length() > 0) encoded.append('&');
            encoded.append(java.net.URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            encoded.append('=').append(java.net.URLEncoder.encode(value(entry.getValue()), StandardCharsets.UTF_8));
        }
        return encoded.toString();
    }

    static String normalizeRegistration(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private Gender parseGender(String value) {
        if (value == null) return null;
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "macho" -> Gender.MACHO;
            case "femea" -> Gender.FEMEA;
            default -> null;
        };
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }

    private AbccMalformedResponseException malformed(String message) {
        return new AbccMalformedResponseException(message);
    }

    private AbccMalformedResponseException malformed(String message, Throwable cause) {
        return new AbccMalformedResponseException(message, cause);
    }
}
