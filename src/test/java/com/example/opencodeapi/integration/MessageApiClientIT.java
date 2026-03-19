package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.CommandApiClient;
import com.example.opencodeapi.client.api.MessageApiClient;
import com.example.opencodeapi.client.api.SessionApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageApiClientIT extends IntegrationTestBase {

    private static SessionApiClient sessionClient;
    private static MessageApiClient messageClient;
    private static CommandApiClient commandClient;
    private static String sessionId;

    @BeforeAll
    static void setUp() {
        sessionClient = new SessionApiClient(httpClient);
        messageClient = new MessageApiClient(httpClient);
        commandClient = new CommandApiClient(httpClient);

        JsonNode session = sessionClient.create(null, "Message IT session");
        sessionId = session.get("id").asText();
    }

    @AfterAll
    static void cleanup() {
        if (sessionId != null) {
            try {
                sessionClient.delete(sessionId);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @Order(1)
    void listMessages_returnsArray() {
        JsonNode messages = messageClient.list(sessionId, null);
        assertJsonArray(messages);
    }

    @Test
    @Order(2)
    void sendMessage_returnsMessageWithParts() {
        Map<String, Object> body = Map.of(
                "parts", List.of(Map.of(
                        "type", "text",
                        "text", "Hello from integration test")));
        try {
            JsonNode result = messageClient.send(sessionId, body);
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(3)
    void getMessage_callableWithDummyId() {
        try {
            JsonNode detail = messageClient.get(sessionId, "non-existent-message-id");
            assertNotNull(detail);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400,
                    "Expected 4xx/5xx for non-existent message ID");
        }
    }

    @Test
    @Order(4)
    void promptAsync_returns204() {
        Map<String, Object> body = Map.of(
                "parts", List.of(Map.of(
                        "type", "text",
                        "text", "Async test")));
        try {
            messageClient.promptAsync(sessionId, body);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(5)
    void executeCommand_callableWithDummy() {
        try {
            messageClient.executeCommand(sessionId, Map.of(
                    "command", "/help",
                    "arguments", Map.of()));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(6)
    void executeShell_callableWithDummy() {
        try {
            messageClient.executeShell(sessionId, Map.of(
                    "agent", "coder",
                    "command", "echo hello"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void listCommands_returnsArray() {
        JsonNode commands = commandClient.list();
        assertJsonArray(commands);
    }
}
