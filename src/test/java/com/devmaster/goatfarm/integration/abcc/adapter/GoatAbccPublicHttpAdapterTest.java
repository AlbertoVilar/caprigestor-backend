package com.devmaster.goatfarm.integration.abcc.adapter;

import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoatAbccPublicHttpAdapterTest {

    @Test
    void preservesTheAlphabeticSuffixOfAnAbccRegistration() {
        assertThat(GoatAbccPublicHttpAdapter.normalizeRegistration(" 1635719026a "))
                .isEqualTo("1635719026A");
    }

    @Test
    void retriesTransientServerErrorsWithinTheConfiguredBound() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> first = response(503, "temporarily unavailable");
        HttpResponse<String> second = response(200, "<html>ok</html>");
        when(client.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(first, second);

        AbccHttpTransport transport = new AbccHttpTransport(client, Duration.ofSeconds(1), 2, 0, 1024);

        assertThat(transport.get("https://example.test")).contains("ok");
        verify(client, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void mapsTimeoutAfterRetriesToTypedException() throws Exception {
        HttpClient client = mock(HttpClient.class);
        when(client.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.net.http.HttpTimeoutException("timeout"));

        AbccHttpTransport transport = new AbccHttpTransport(client, Duration.ofSeconds(1), 2, 0, 1024);

        assertThatThrownBy(() -> transport.get("https://example.test"))
                .isInstanceOf(AbccTimeoutException.class);
        verify(client, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void rejectsResponsesAboveTheConfiguredLimit() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> oversized = response(200, "12345");
        when(client.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(oversized);
        AbccHttpTransport transport = new AbccHttpTransport(client, Duration.ofSeconds(1), 1, 0, 4);

        assertThatThrownBy(() -> transport.get("https://example.test"))
                .isInstanceOf(AbccMalformedResponseException.class);
    }

    @Test
    void encodesAffixWithSpacesForPageOneAndPageTwoRequests() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse<String> ok = response(200, "ok");
        when(client.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(ok);

        GoatAbccPublicHttpAdapter adapter = new GoatAbccPublicHttpAdapter();
        String currentUrl = "https://siscapri.abccaprinos.com.br/x.php?"
                + "m=siscapri.Genealogia&f=Buscar&i_keyword_2=CAPRIL VILAR&i_keyword_1=16432";
        String pageOneUrl = composePagingUrl(adapter, currentUrl, 1);
        String pageTwoUrl = composePagingUrl(adapter, currentUrl, 2);

        AbccHttpTransport transport = new AbccHttpTransport(client, Duration.ofSeconds(1), 1, 0, 1024);
        transport.post(pageOneUrl, "viewstate=token");
        transport.post(pageTwoUrl, "viewstate=token");

        var requests = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(client, times(2)).send(requests.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(requests.getAllValues().get(0).uri().getRawQuery())
                .contains("i_keyword_2=CAPRIL%20VILAR")
                .contains("curpage=1");
        assertThat(requests.getAllValues().get(1).uri().getRawQuery())
                .contains("i_keyword_2=CAPRIL%20VILAR")
                .contains("curpage=2");
    }

    @Test
    void preservesNoSpaceAffixAndExistingEncodingWithoutDoubleEncoding() {
        GoatAbccPublicHttpAdapter adapter = new GoatAbccPublicHttpAdapter();

        String noSpaceUrl = composePagingUrl(
                adapter,
                "https://siscapri.abccaprinos.com.br/x.php?i_keyword_2=CRS",
                2
        );
        String alreadyEncodedUrl = composePagingUrl(
                adapter,
                "https://siscapri.abccaprinos.com.br/x.php?i_keyword_2="
                        + URLEncoder.encode("CAPRIL VILAR", StandardCharsets.UTF_8),
                2
        );

        assertThat(noSpaceUrl).contains("i_keyword_2=CRS");
        assertThat(alreadyEncodedUrl)
                .contains("i_keyword_2=CAPRIL%20VILAR")
                .doesNotContain("CAPRIL%2520VILAR");
    }

    @Test
    void carriesFiltersToTheRequestedPageThroughTheAdapter() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpClient.Builder clientBuilder = mock(HttpClient.Builder.class);
        HttpResponse<String> searchPage = response(200,
                "<xmp id=\"viewstate\" style=\"display:none\">token</xmp>");
        HttpResponse<String> firstPage = response(200, searchResultPage(1, 2,
                "https://siscapri.abccaprinos.com.br/x.php?i_keyword_2=CAPRIL VILAR"));
        HttpResponse<String> secondPage = response(200, searchResultPage(2, 2,
                "https://siscapri.abccaprinos.com.br/x.php?i_keyword_2=CAPRIL VILAR",
                "external-2"));
        when(client.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(searchPage, firstPage, secondPage);
        when(clientBuilder.cookieHandler(any())).thenReturn(clientBuilder);
        when(clientBuilder.followRedirects(any())).thenReturn(clientBuilder);
        when(clientBuilder.connectTimeout(any())).thenReturn(clientBuilder);
        when(clientBuilder.build()).thenReturn(client);

        var adapter = new GoatAbccPublicHttpAdapter();
        try (var ignored = mockStatic(HttpClient.class)) {
            ignored.when(HttpClient::newBuilder).thenReturn(clientBuilder);

            var result = adapter.search(GoatAbccSearchRequestVO.builder()
                    .raceId(3)
                    .affix("CAPRIL VILAR")
                    .page(2)
                    .build());

            assertThat(result.getCurrentPage()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getItems()).extracting(item -> item.getExternalId())
                    .containsExactly("external-2");
        }

        var requests = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(client, times(3)).send(requests.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(requests.getAllValues().get(1).uri().toString())
                .isEqualTo("https://siscapri.abccaprinos.com.br/x.php?m=siscapri.Genealogia"
                        + "&f=Buscar&site=siscapri&runat=client");
        assertThat(requests.getAllValues().get(2).uri().getRawQuery())
                .contains("i_keyword_2=CAPRIL%20VILAR")
                .contains("curpage=2");
    }

    private String searchResultPage(int currentPage, int totalPages, String currentUrl, String... externalIds) {
        String rows = java.util.Arrays.stream(externalIds)
                .map(this::searchResultRow)
                .reduce("", String::concat);
        return "<div id=\"lvListaGenealogia\" class=\"ListView\" curpage=\"" + currentPage
                + "\" pages=\"" + totalPages + "\" offset=\"7\" current_url=\""
                + currentUrl + "\"><div class=\"dataset\"><table><tbody>" + rows
                + "</tbody></table></div></div>";
    }

    private String searchResultRow(String externalId) {
        return "<tr><td><input name=\"valueid\" value=\"" + externalId + "\"></td>"
                + "<td></td><td>Animal</td><td></td><td></td><td>16432</td>"
                + "<td>26001</td><td></td><td>CAPRIL VILAR</td><td></td><td>FÊMEA</td>"
                + "<td>ALPINA</td><td></td></tr>";
    }

    private String composePagingUrl(GoatAbccPublicHttpAdapter adapter, String currentUrl, int page) {
        return ReflectionTestUtils.invokeMethod(adapter, "composePagingUrl", currentUrl, page, 7);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }
}
