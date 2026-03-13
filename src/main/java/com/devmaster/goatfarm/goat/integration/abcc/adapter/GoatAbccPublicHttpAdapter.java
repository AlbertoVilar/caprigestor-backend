package com.devmaster.goatfarm.goat.integration.abcc.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GoatAbccPublicHttpAdapter implements GoatAbccPublicQueryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoatAbccPublicHttpAdapter.class);
    private static final String BASE_URL = "https://siscapri.abccaprinos.com.br";
    private static final String SEARCH_PAGE_URL = BASE_URL + "/x.php?m=siscapri.genealogia&site=siscapri";
    private static final String SEARCH_URL = BASE_URL + "/x.php?m=siscapri.Genealogia&f=Buscar&site=siscapri&runat=client";
    private static final String PREVIEW_URL = BASE_URL + "/x.php?m=siscapri.genealogia&f=mostraArvoreGenealogica&site=siscapri&runat=client";

    private static final Pattern VIEWSTATE_PATTERN = Pattern.compile(
            "<xmp id=\"viewstate\" style=\"display:none\">(.*?)</xmp>",
            Pattern.DOTALL
    );

    @Override
    public List<GoatAbccRaceOptionVO> listRaces() {
        try {
            HttpClient client = newClient();
            String searchPage = get(client, SEARCH_PAGE_URL);
            return parseRaceOptions(searchPage);
        } catch (Exception ex) {
            LOGGER.warn("Falha ao carregar lista de raças da ABCC pública.", ex);
            throw new RuntimeException("Falha ao carregar lista de raças da ABCC pública.", ex);
        }
    }

    @Override
    public GoatAbccRawSearchResultVO search(GoatAbccSearchRequestVO requestVO) {
        try {
            HttpClient client = newClient();
            String searchPage = get(client, SEARCH_PAGE_URL);
            String viewstate = extractViewstate(searchPage);
            pause();

            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("viewstate", viewstate);
            payload.put("i_keyword_0", String.valueOf(requestVO.getRaceId()));
            payload.put("i_keyword_2", requestVO.getAffix());
            payload.put("i_keyword_6", valueOrEmpty(requestVO.getSex()));
            payload.put("i_keyword_1", valueOrEmpty(requestVO.getTod()));
            payload.put("i_keyword_3", valueOrEmpty(requestVO.getToe()));
            payload.put("i_keyword_5", valueOrEmpty(requestVO.getDna()));
            payload.put("i_keyword_4", valueOrEmpty(requestVO.getName()));

            String resultHtml = post(client, SEARCH_URL, payload);
            GoatAbccRawSearchResultVO firstPage = parseSearchResult(resultHtml);

            int targetPage = requestVO.getPage() == null ? 1 : requestVO.getPage();
            if (targetPage <= 1 || firstPage.getTotalPages() == null || firstPage.getTotalPages() <= 1) {
                return firstPage;
            }

            int boundedPage = Math.min(targetPage, firstPage.getTotalPages());
            if (boundedPage == firstPage.getCurrentPage()) {
                return firstPage;
            }

            String pagingUrl = composePagingUrl(
                    firstPage.getCurrentUrl(),
                    boundedPage,
                    firstPage.getOffset() == null ? 7 : firstPage.getOffset()
            );
            pause();
            String pagedHtml = post(client, pagingUrl, Map.of("viewstate", viewstate));
            return parseSearchResult(pagedHtml);
        } catch (Exception ex) {
            LOGGER.warn("Falha ao buscar animais na ABCC pública.", ex);
            throw new RuntimeException("Falha ao buscar dados da ABCC pública.", ex);
        }
    }

    @Override
    public GoatAbccRawPreviewVO preview(String externalId) {
        try {
            HttpClient client = newClient();
            String searchPage = get(client, SEARCH_PAGE_URL);
            String viewstate = extractViewstate(searchPage);
            pause();

            String previewHtml = post(client, PREVIEW_URL, Map.of(
                    "viewstate", viewstate,
                    "valueid", externalId
            ));
            return parsePreview(externalId, previewHtml);
        } catch (Exception ex) {
            LOGGER.warn("Falha ao carregar preview de genealogia ABCC para externalId={}", externalId, ex);
            throw new RuntimeException("Falha ao carregar preview da ABCC pública.", ex);
        }
    }

    private HttpClient newClient() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        return HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    private List<GoatAbccRaceOptionVO> parseRaceOptions(String html) {
        Document document = Jsoup.parse(html);
        Element select = document.selectFirst("select[name=i_keyword_0]");
        if (select == null) {
            return List.of();
        }

        Map<Integer, String> deduplicated = new LinkedHashMap<>();
        for (Element option : select.select("option")) {
            String idValue = cleanText(option.attr("value"));
            String name = cleanText(option.text());
            if (idValue == null || name == null) {
                continue;
            }

            try {
                Integer id = Integer.parseInt(idValue);
                deduplicated.put(id, name);
            } catch (NumberFormatException ignored) {
                // ignora opções não numéricas
            }
        }

        return deduplicated.entrySet().stream()
                .map(entry -> GoatAbccRaceOptionVO.builder()
                        .id(entry.getKey())
                        .name(entry.getValue())
                        .build())
                .toList();
    }

    private GoatAbccRawSearchResultVO parseSearchResult(String html) {
        Document document = Jsoup.parse(html);
        Element listView = document.selectFirst("div#lvListaGenealogia.ListView, div.ListView");
        if (listView == null) {
            return GoatAbccRawSearchResultVO.builder()
                    .currentPage(1)
                    .totalPages(1)
                    .offset(7)
                    .currentUrl(null)
                    .items(List.of())
                    .build();
        }

        int currentPage = parseInteger(listView.attr("curpage"), 1);
        int totalPages = parseInteger(listView.attr("pages"), 1);
        int offset = parseInteger(listView.attr("offset"), 7);
        String currentUrl = Entities.unescape(listView.attr("current_url"));

        List<GoatAbccRawSearchItemVO> items = new ArrayList<>();
        Elements rows = listView.select("div.dataset table tbody tr");
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() < 13) {
                continue;
            }
            String externalId = textOrNull(row.selectFirst("input[name=valueid]"), "value");
            if (externalId == null) {
                externalId = cleanText(cells.get(1).text());
            }

            items.add(GoatAbccRawSearchItemVO.builder()
                    .externalId(externalId)
                    .nome(cleanText(cells.get(2).text()))
                    .situacao(cleanText(cells.get(3).text()))
                    .dna(cleanText(cells.get(4).text()))
                    .tod(cleanText(cells.get(5).text()))
                    .toe(cleanText(cells.get(6).text()))
                    .criador(cleanText(cells.get(7).text()))
                    .afixo(cleanText(cells.get(8).text()))
                    .dataNascimento(cleanText(cells.get(9).text()))
                    .sexo(cleanText(cells.get(10).text()))
                    .raca(cleanText(cells.get(11).text()))
                    .pelagem(cleanText(cells.get(12).text()))
                    .build());
        }

        return GoatAbccRawSearchResultVO.builder()
                .currentPage(currentPage)
                .totalPages(totalPages)
                .offset(offset)
                .currentUrl(currentUrl)
                .items(items)
                .build();
    }

    private GoatAbccRawPreviewVO parsePreview(String externalId, String html) {
        Document document = Jsoup.parse(html);
        Elements boxes = document.select("div#divArvore div.bordaBox table");
        if (boxes.isEmpty()) {
            throw new IllegalStateException("ABCC retornou HTML sem dados de genealogia.");
        }

        Map<String, String> principal = parseKeyValues(boxes.getFirst());

        String fatherName = null;
        String fatherRegistration = null;
        String motherName = null;
        String motherRegistration = null;

        for (int i = 1; i < boxes.size(); i++) {
            Map<String, String> relative = parseKeyValues(boxes.get(i));
            String relationship = normalizeToken(relative.get("Parentesco"));
            if ("pai".equals(relationship)) {
                fatherName = cleanText(relative.get("Nome"));
                fatherRegistration = cleanText(relative.get("Registro"));
            } else if ("mae".equals(relationship)) {
                motherName = cleanText(relative.get("Nome"));
                motherRegistration = cleanText(relative.get("Registro"));
            }
        }

        return GoatAbccRawPreviewVO.builder()
                .externalId(externalId)
                .nome(cleanText(principal.get("Nome")))
                .registro(cleanText(principal.get("Registro")))
                .criador(cleanText(principal.get("Criador")))
                .proprietario(cleanText(principal.get("Proprietário")))
                .raca(cleanText(principal.get("Raça")))
                .pelagem(cleanText(principal.get("Pelagem")))
                .situacao(cleanText(principal.get("Situação")))
                .sexo(cleanText(principal.get("Sexo")))
                .categoria(cleanText(principal.get("Categoria")))
                .tod(cleanText(principal.get("TOD")))
                .toe(cleanText(principal.get("TOE")))
                .dataNascimento(cleanText(principal.get("Data Nasc.")))
                .paiNome(fatherName)
                .paiRegistro(fatherRegistration)
                .maeNome(motherName)
                .maeRegistro(motherRegistration)
                .build();
    }

    private Map<String, String> parseKeyValues(Element table) {
        Map<String, String> values = new HashMap<>();
        for (Element row : table.select("tr")) {
            Elements columns = row.select("td");
            if (columns.size() < 2) {
                continue;
            }
            String key = cleanText(columns.get(0).text());
            if (key == null) {
                continue;
            }
            values.put(key.replace(":", ""), cleanText(columns.get(1).text()));
        }
        return values;
    }

    private String composePagingUrl(String currentUrl, int page, int offset) {
        String base = currentUrl == null ? "" : currentUrl;
        if (!base.startsWith("http")) {
            base = BASE_URL + base;
        }
        return base
                + "&curpage=" + page
                + "&offset=" + offset
                + "&_lvid=lvListaGenealogia"
                + "&paging=true"
                + "&runat=client";
    }

    private String extractViewstate(String html) {
        Matcher matcher = VIEWSTATE_PATTERN.matcher(Objects.requireNonNullElse(html, ""));
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String get(HttpClient client, String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP GET ABCC retornou status " + response.statusCode());
        }
        return response.body();
    }

    private String post(HttpClient client, String url, Map<String, String> form) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(formEncode(form), StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP POST ABCC retornou status " + response.statusCode());
        }
        return response.body();
    }

    private String formEncode(Map<String, String> form) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append('=');
            builder.append(URLEncoder.encode(valueOrEmpty(entry.getValue()), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private void pause() {
        try {
            Thread.sleep(180);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private int parseInteger(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String textOrNull(Element element, String attribute) {
        if (element == null) {
            return null;
        }
        String value = attribute == null ? element.text() : element.attr(attribute);
        return cleanText(value);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace('\u00A0', ' ').trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(java.util.Locale.ROOT);
    }
}
