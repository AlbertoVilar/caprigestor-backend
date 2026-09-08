package com.devmaster.goatfarm.goat.integration.abcc.adapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Small transport boundary for ABCC calls. It owns timeouts, bounded retries and
 * conversion of low-level failures into application-level typed exceptions.
 */
final class AbccHttpTransport {

    private static final int MAX_RESPONSE_BYTES_HARD_LIMIT = 10 * 1024 * 1024;

    private final HttpClient client;
    private final Duration requestTimeout;
    private final int maxAttempts;
    private final long retryBackoffMillis;
    private final int maxResponseBytes;

    AbccHttpTransport(
            HttpClient client,
            Duration requestTimeout,
            int maxAttempts,
            long retryBackoffMillis,
            int maxResponseBytes
    ) {
        this.client = client;
        this.requestTimeout = requestTimeout;
        this.maxAttempts = Math.max(1, Math.min(maxAttempts, 3));
        this.retryBackoffMillis = Math.max(0, Math.min(retryBackoffMillis, 5_000));
        this.maxResponseBytes = Math.max(1, Math.min(maxResponseBytes, MAX_RESPONSE_BYTES_HARD_LIMIT));
    }

    String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(requestTimeout)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9")
                .build();
        return send(request, "GET");
    }

    String post(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .timeout(requestTimeout)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        return send(request, "POST");
    }

    private String send(HttpRequest request, String method) throws IOException, InterruptedException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
                );
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return requireSafeBody(response.body());
                }

                boolean retryable = status >= 500 && attempt < maxAttempts;
                if (retryable) {
                    backoff(attempt);
                    continue;
                }
                throw new AbccUnavailableException(
                        "ABCC retornou status HTTP " + status + " para " + method + ".", null
                );
            } catch (HttpTimeoutException ex) {
                if (attempt < maxAttempts) {
                    backoff(attempt);
                    continue;
                }
                throw new AbccTimeoutException("Tempo limite excedido ao consultar a ABCC pública.", ex);
            } catch (IOException ex) {
                if (attempt < maxAttempts) {
                    backoff(attempt);
                    continue;
                }
                throw new AbccUnavailableException("Falha de comunicação com a ABCC pública.", ex);
            }
        }
        throw new AbccUnavailableException("Falha de comunicação com a ABCC pública.", null);
    }

    private String requireSafeBody(String body) {
        String safeBody = body == null ? "" : body;
        int size = safeBody.getBytes(StandardCharsets.UTF_8).length;
        if (size > maxResponseBytes) {
            throw new AbccMalformedResponseException("Resposta da ABCC excede o limite permitido.");
        }
        return safeBody;
    }

    private void backoff(int attempt) throws InterruptedException {
        if (retryBackoffMillis == 0) {
            return;
        }
        Thread.sleep(retryBackoffMillis * attempt);
    }
}
