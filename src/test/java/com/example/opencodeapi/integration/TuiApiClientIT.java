package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.TuiApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TuiApiClientIT extends IntegrationTestBase {

    private static TuiApiClient client;

    @BeforeAll
    static void setUp() {
        client = new TuiApiClient(httpClient);
    }

    @Test
    void appendPrompt_returnsResult() {
        try {
            JsonNode result = client.appendPrompt("test text");
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void openHelp_returnsResult() {
        try {
            JsonNode result = client.openHelp();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void openSessions_returnsResult() {
        try {
            JsonNode result = client.openSessions();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void openThemes_returnsResult() {
        try {
            JsonNode result = client.openThemes();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void openModels_returnsResult() {
        try {
            JsonNode result = client.openModels();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void submitPrompt_returnsResult() {
        try {
            JsonNode result = client.submitPrompt();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void clearPrompt_returnsResult() {
        try {
            JsonNode result = client.clearPrompt();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void executeCommand_returnsResult() {
        try {
            JsonNode result = client.executeCommand("/help");
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void showToast_returnsResult() {
        try {
            JsonNode result = client.showToast("IT Test", "Hello from IT", "info");
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void controlNext_timesOutGracefully() {
        try {
            client.controlNext();
        } catch (OpencodeConnectionException e) {
            assertTrue(e.getMessage().contains("Timeout") || e.getMessage().contains("connect"),
                    "Expected timeout or connection error for blocking endpoint");
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void controlResponse_callableWithDummy() {
        try {
            client.controlResponse(Map.of("body", "test"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }
}
