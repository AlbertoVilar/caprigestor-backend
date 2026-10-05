package com.devmaster.goatfarm.integration.abcc.adapter;

import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    void preservesLocalPagingSemanticsForTheCurrentJsonApi() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpClient.Builder clientBuilder = mock(HttpClient.Builder.class);
        when(clientBuilder.cookieHandler(any())).thenReturn(clientBuilder);
        when(clientBuilder.followRedirects(any())).thenReturn(clientBuilder);
        when(clientBuilder.connectTimeout(any())).thenReturn(clientBuilder);
        when(clientBuilder.build()).thenReturn(client);
        String searchJson = "[" +
                "{\"id\":\"1\",\"registro\":\"1400800001\",\"nome\":\"A1\"}," +
                "{\"id\":\"2\",\"registro\":\"1400800002\",\"nome\":\"A2\"}," +
                "{\"id\":\"3\",\"registro\":\"1400800003\",\"nome\":\"A3\"}," +
                "{\"id\":\"4\",\"registro\":\"1400800004\",\"nome\":\"A4\"}," +
                "{\"id\":\"5\",\"registro\":\"1400800005\",\"nome\":\"A5\"}," +
                "{\"id\":\"6\",\"registro\":\"1400800006\",\"nome\":\"A6\"}," +
                "{\"id\":\"7\",\"registro\":\"1400800007\",\"nome\":\"A7\"}," +
                "{\"id\":\"8\",\"registro\":\"1400800008\",\"nome\":\"A8\"}," +
                "{\"id\":\"9\",\"registro\":\"1400800009\",\"nome\":\"A9\"}," +
                "{\"id\":\"10\",\"registro\":\"1400800010\",\"nome\":\"A10\"}," +
                "{\"id\":\"11\",\"registro\":\"1400800011\",\"nome\":\"A11\"}]";
        HttpResponse<String> searchResponse = response(200, searchJson);
        when(client.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(searchResponse);

        GoatAbccPublicHttpAdapter adapter = new GoatAbccPublicHttpAdapter();
        try (var ignored = mockStatic(HttpClient.class)) {
            ignored.when(HttpClient::newBuilder).thenReturn(clientBuilder);
            var result = adapter.search(GoatAbccSearchRequestVO.builder()
                    .raceId(3).affix("CAPRIL VILAR").page(2).build());
            assertThat(result.getCurrentPage()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getOffset()).isEqualTo(10);
            assertThat(result.getItems()).extracting(item -> item.getNome()).containsExactly("A11");
        }

        var requests = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(client, times(1)).send(requests.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(requests.getValue().uri().toString())
                .isEqualTo("https://siscapri.abccaprinos.com.br/x.php?m=siscapri.api.api_genealogia&site=siscapri");
    }

    @Test
    void mapsCurrentJsonSearchResponseThroughTheAdapter() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpClient.Builder clientBuilder = mock(HttpClient.Builder.class);
        HttpResponse<String> searchResponse = response(200,
                "[{\"id\":\"external-2\",\"registro\":\"1643226001\","
                        + "\"nome\":\"ANIMAL\",\"afixo\":\"CAPRIL VILAR\","
                        + "\"situacao\":\"RGD\",\"dna\":\"Confirmado\","
                        + "\"criador\":\"CRIADOR\",\"data_nasce\":\"01/02/2020\","
                        + "\"sexo\":\"FÊMEA\",\"raca\":\"ALPINA\",\"pelagem\":\"Branca\"}]");
        when(client.<String>send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(searchResponse);
        when(clientBuilder.cookieHandler(any())).thenReturn(clientBuilder);
        when(clientBuilder.followRedirects(any())).thenReturn(clientBuilder);
        when(clientBuilder.connectTimeout(any())).thenReturn(clientBuilder);
        when(clientBuilder.build()).thenReturn(client);

        var adapter = new GoatAbccPublicHttpAdapter();
        try (var ignored = mockStatic(HttpClient.class)) {
            ignored.when(HttpClient::newBuilder).thenReturn(clientBuilder);

            var result = adapter.search(GoatAbccSearchRequestVO.builder().raceId(3)
                    .affix("CAPRIL VILAR").page(2).build());

            assertThat(result.getCurrentPage()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getItems()).extracting(item -> item.getExternalId())
                    .containsExactly("external-2");
            assertThat(result.getItems().getFirst().getTod()).isEqualTo("16432");
            assertThat(result.getItems().getFirst().getToe()).isEqualTo("26001");
            assertThat(result.getItems().getFirst().getSituacao()).isEqualTo("RGD");
            assertThat(result.getItems().getFirst().getDna()).isEqualTo("Confirmado");
            assertThat(result.getItems().getFirst().getCriador()).isEqualTo("CRIADOR");
            assertThat(result.getItems().getFirst().getDataNascimento()).isEqualTo("01/02/2020");
            assertThat(result.getItems().getFirst().getPelagem()).isEqualTo("Branca");
        }

        var requests = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(client, times(1)).send(requests.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(requests.getValue().uri().toString())
                .isEqualTo("https://siscapri.abccaprinos.com.br/x.php?m=siscapri.api.api_genealogia&site=siscapri");
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }
}
