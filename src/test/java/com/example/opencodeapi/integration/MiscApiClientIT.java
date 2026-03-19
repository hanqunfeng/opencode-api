package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.*;
import com.example.opencodeapi.client.http.OpencodeApiException;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MiscApiClientIT extends IntegrationTestBase {

    private static AgentApiClient agentClient;
    private static LogApiClient logClient;
    private static AuthApiClient authClient;
    private static EventApiClient eventClient;
    private static DocApiClient docClient;

    @BeforeAll
    static void setUp() {
        agentClient = new AgentApiClient(httpClient);
        logClient = new LogApiClient(httpClient);
        authClient = new AuthApiClient(httpClient);
        eventClient = new EventApiClient(httpClient);
        docClient = new DocApiClient(httpClient);
    }

    @Test
    void listAgents_returnsArray() {
        JsonNode agents = agentClient.list();
        assertJsonArray(agents);
    }

    @Test
    void writeLog_returnsResult() {
        JsonNode result = logClient.write("integration-test", "info", "Test log entry", null);
        assertNotNull(result);
    }

    @Test
    void setAuth_callableWithDummy() {
        try {
            authClient.set("dummy-provider", Map.of("token", "dummy"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void getEvent_receivesFirstSseEvent() {
        String event = eventClient.getEvent();
        assertSseEvent(event);
    }

    @Test
    void getDoc_returnsHtml() {
        String html = docClient.get();
        assertNotNull(html);
        assertFalse(html.isBlank());
    }
}
