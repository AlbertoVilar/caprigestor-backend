package com.devmaster.goatfarm.goat.integration.abcc.adapter;

import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }
}
