package com.devmaster.goatfarm.integration.abcc.adapter;

import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoatAbccPublicHttpAdapterContractTest {

    private HttpServer server;
    private GoatAbccPublicHttpAdapter adapter;
    private volatile String lastRequestBody;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            lastRequestBody = body;
            String operation = body.contains("f=getRacas") ? "races"
                    : body.contains("f=getDetalhesAnimalCompleto") ? "details" : "search";
            String response = switch (operation) {
                case "races" -> fixture("abcc/get-racas.json");
                case "details" -> fixture("abcc/details-xeque.json");
                default -> fixture("abcc/search-xeque.json");
            };
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        adapter = new GoatAbccPublicHttpAdapter("http://localhost:" + server.getAddress().getPort() + "/api");
    }

    @AfterEach
    void tearDown() {
        if (server != null) server.stop(0);
    }

    @Test
    void mapsCurrentRaceJsonAndSkipsMalformedEntries() {
        var races = adapter.listRaces();

        assertThat(races).extracting("id").containsExactly(3, 14);
        assertThat(races).extracting("name").containsExactly("ALPINA", "SAANEN");
    }

    @Test
    void mapsCurrentSearchJsonAndPreservesAlphabeticRegistrationSuffix() {
        var result = adapter.search(GoatAbccSearchRequestVO.builder()
                .raceId(3)
                .name("XEQUE")
                .affix("CAPRIL VILAR")
                .sex("Macho")
                .dna("Confirmado")
                .tod("16432")
                .toe("18012")
                .build());

        var form = decodeForm(lastRequestBody);
        assertThat(form).containsEntry("runat", "client");
        assertThat(form).containsEntry("f", "getAnimaisPublico");
        assertThat(form).containsEntry("raca", "3");
        assertThat(form).containsEntry("termo", "XEQUE");
        assertThat(form).containsEntry("afixo", "CAPRIL VILAR");
        assertThat(form).containsEntry("sexo", "Macho");
        assertThat(form).containsEntry("dna", "Confirmado");
        assertThat(form).containsEntry("tod", "16432");
        assertThat(form).containsEntry("toe", "18012");

        var suffix = adapter.searchByRegistration(null, "1643218012A");

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().getFirst().getExternalId()).isEqualTo("55878");
        assertThat(result.getItems().getFirst().getTod()).isEqualTo("16432");
        assertThat(result.getItems().getFirst().getToe()).isEqualTo("18012");
        assertThat(suffix.getItems()).hasSize(1);
        assertThat(suffix.getItems().getFirst().getExternalId()).isEqualTo("55879");
        assertThat(suffix.getItems().getFirst().getTod()).isEqualTo("16432");
        assertThat(suffix.getItems().getFirst().getToe()).isEqualTo("18012A");
    }

    @Test
    void mapsPreviewAndCompleteGenealogyFromCurrentJson() {
        var preview = adapter.preview("55878");
        var genealogy = adapter.findGenealogyByRegistrationNumber("1643218012");

        assertThat(preview.getRegistro()).isEqualTo("1643218012");
        assertThat(preview.getPaiRegistro()).isEqualTo("1635717065");
        assertThat(genealogy).isPresent();
        assertThat(genealogy.get().getFatherName()).isEqualTo("C.V.C SIGNOS PETROLEO");
        assertThat(genealogy.get().getPaternalGrandfatherRegistrationNumber()).isEqualTo("1422915618");
        assertThat(genealogy.get().getBisavoMaternaMaeRegistrationNumber()).isEqualTo("2114510040");
    }

    @Test
    void rejectsReturnedRegistrationThatDoesNotExactlyMatchRequestedRegistration() {
        assertThat(adapter.findGenealogyByRegistrationNumber("1643218012A")).isEmpty();
    }

    @Test
    void malformedJsonIsTypedAsMalformedResponse() throws IOException {
        server.removeContext("/api");
        server.createContext("/api", exchange -> {
            byte[] bytes = "{not-json".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        assertThatThrownBy(adapter::listRaces)
                .isInstanceOf(com.devmaster.goatfarm.integration.abcc.adapter.AbccMalformedResponseException.class);
    }

    private String fixture(String resource) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("fixture not found: " + resource);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> decodeForm(String body) {
        return Arrays.stream(body.split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair -> URLDecoder.decode(pair.length == 2 ? pair[1] : "", StandardCharsets.UTF_8)));
    }
}
