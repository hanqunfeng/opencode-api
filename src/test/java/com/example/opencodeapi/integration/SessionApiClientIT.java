package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.SessionApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

import java.util.Map;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionApiClientIT extends IntegrationTestBase {

    private static SessionApiClient client;
    private static String testSessionId;

    @BeforeAll
    static void setUp() {
        client = new SessionApiClient(httpClient);
    }

    @AfterAll
    static void cleanup() {
        if (testSessionId != null) {
            try {
                client.delete(testSessionId);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @Order(1)
    void createSession_returnsSession() {
        JsonNode session = client.create(null, "IT test session");
        assertNotNull(session);
        assertJsonHasField(session, "id");
        testSessionId = session.get("id").asText();
    }

    @Test
    @Order(2)
    void listSessions_returnsArray() {
        JsonNode sessions = client.list();
        assertJsonArray(sessions);
    }

    @Test
    @Order(3)
    void getSessionStatus_returnsStatus() {
        JsonNode status = client.getStatus();
        assertNotNull(status);
    }

    @Test
    @Order(4)
    void getSession_returnsSessionDetail() {
        assertNotNull(testSessionId, "Session must be created first");
        JsonNode session = client.get(testSessionId);
        assertNotNull(session);
        assertJsonHasField(session, "id");
    }

    @Test
    @Order(5)
    void patchSession_updatesTitle() {
        assertNotNull(testSessionId);
        JsonNode updated = client.patch(testSessionId, Map.of("title", "Updated title"));
        assertNotNull(updated);
    }

    @Test
    @Order(6)
    void getSessionChildren_returnsArray() {
        assertNotNull(testSessionId);
        JsonNode children = client.getChildren(testSessionId);
        assertJsonArray(children);
    }

    @Test
    @Order(7)
    void getSessionTodo_returnsArray() {
        assertNotNull(testSessionId);
        JsonNode todos = client.getTodo(testSessionId);
        assertJsonArray(todos);
    }

    @Test
    @Order(8)
    void getSessionDiff_returnsArray() {
        assertNotNull(testSessionId);
        JsonNode diff = client.getDiff(testSessionId, null);
        assertJsonArray(diff);
    }

    @Test
    @Order(9)
    void forkSession_createsForkedSession() {
        assertNotNull(testSessionId);
        JsonNode forked = client.fork(testSessionId, null);
        assertNotNull(forked);
        assertJsonHasField(forked, "id");
        String forkedId = forked.get("id").asText();
        try {
            client.delete(forkedId);
        } catch (Exception ignored) {
        }
    }

    @Test
    @Order(10)
    void abortSession_returnsResult() {
        assertNotNull(testSessionId);
        JsonNode result = client.abort(testSessionId);
        assertNotNull(result);
    }

    @Test
    @Order(11)
    void shareSession_returnsResult() {
        assertNotNull(testSessionId);
        try {
            JsonNode result = client.share(testSessionId);
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(12)
    void unshareSession_returnsResult() {
        assertNotNull(testSessionId);
        try {
            JsonNode result = client.unshare(testSessionId);
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(13)
    void unrevertSession_returnsResult() {
        assertNotNull(testSessionId);
        JsonNode result = client.unrevert(testSessionId);
        assertNotNull(result);
    }

    @Test
    @Order(14)
    void initSession_callableWithDummyParams() {
        assertNotNull(testSessionId);
        try {
            client.init(testSessionId, Map.of(
                    "messageID", "dummy",
                    "providerID", "dummy",
                    "modelID", "dummy"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(15)
    void summarizeSession_callableWithDummyParams() {
        assertNotNull(testSessionId);
        try {
            client.summarize(testSessionId, Map.of(
                    "providerID", "dummy",
                    "modelID", "dummy"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(16)
    void revertSession_callableWithDummyParams() {
        assertNotNull(testSessionId);
        try {
            client.revert(testSessionId, Map.of("messageID", "dummy"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(17)
    void respondToPermission_callableWithDummyParams() {
        assertNotNull(testSessionId);
        try {
            client.respondToPermission(testSessionId, "dummy-perm",
                    Map.of("response", "allow"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(99)
    void deleteSession_deletesSuccessfully() {
        assertNotNull(testSessionId);
        JsonNode result = client.delete(testSessionId);
        assertNotNull(result);
        testSessionId = null;
    }
}
