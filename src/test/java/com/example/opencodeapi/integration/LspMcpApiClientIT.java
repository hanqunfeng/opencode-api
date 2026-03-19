package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.FormatterApiClient;
import com.example.opencodeapi.client.api.LspApiClient;
import com.example.opencodeapi.client.api.McpApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LspMcpApiClientIT extends IntegrationTestBase {

    private static LspApiClient lspClient;
    private static FormatterApiClient formatterClient;
    private static McpApiClient mcpClient;

    @BeforeAll
    static void setUp() {
        lspClient = new LspApiClient(httpClient);
        formatterClient = new FormatterApiClient(httpClient);
        mcpClient = new McpApiClient(httpClient);
    }

    @Test
    void getLspStatus_returnsResult() {
        JsonNode result = lspClient.getStatus();
        assertNotNull(result);
    }

    @Test
    void getFormatterStatus_returnsResult() {
        JsonNode result = formatterClient.getStatus();
        assertNotNull(result);
    }

    @Test
    void getMcpStatus_returnsResult() {
        JsonNode result = mcpClient.getStatus();
        assertNotNull(result);
    }

    @Test
    void addMcpServer_callableWithDummy() {
        try {
            mcpClient.add("test-mcp", Map.of("command", "echo"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }
}
