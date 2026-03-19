package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.FileApiClient;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

class FileApiClientIT extends IntegrationTestBase {

    private static FileApiClient client;

    @BeforeAll
    static void setUp() {
        client = new FileApiClient(httpClient);
    }

    @Test
    void findInFiles_returnsResults() {
        JsonNode result = client.find("TODO");
        assertNotNull(result);
    }

    @Test
    void findFile_returnsArray() {
        JsonNode result = client.findFile("pom", null, null, null, null);
        assertJsonArray(result);
    }

    @Test
    void findSymbol_returnsResults() {
        JsonNode result = client.findSymbol("main");
        assertNotNull(result);
    }

    @Test
    void listFiles_returnsArray() {
        JsonNode result = client.list(".");
        assertJsonArray(result);
    }

    @Test
    void getFileContent_returnsContent() {
        JsonNode result = client.getContent("pom.xml");
        assertNotNull(result);
    }

    @Test
    void getFileStatus_returnsArray() {
        JsonNode result = client.getStatus();
        assertJsonArray(result);
    }
}
