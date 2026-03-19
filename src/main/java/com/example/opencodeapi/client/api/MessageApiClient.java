package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MessageApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list(String sessionId, Integer limit) {
        Map<String, String> params = limit != null ? Map.of("limit", limit.toString()) : null;
        return httpClient.get("/session/" + sessionId + "/message", params, JsonNode.class);
    }

    public JsonNode send(String sessionId, Map<String, Object> body) {
        return httpClient.post("/session/" + sessionId + "/message", body, JsonNode.class);
    }

    public JsonNode get(String sessionId, String messageId) {
        return httpClient.get("/session/" + sessionId + "/message/" + messageId, JsonNode.class);
    }

    public void promptAsync(String sessionId, Map<String, Object> body) {
        httpClient.postNoContent("/session/" + sessionId + "/prompt_async", body);
    }

    public JsonNode executeCommand(String sessionId, Map<String, Object> body) {
        return httpClient.post("/session/" + sessionId + "/command", body, JsonNode.class);
    }

    public JsonNode executeShell(String sessionId, Map<String, Object> body) {
        return httpClient.post("/session/" + sessionId + "/shell", body, JsonNode.class);
    }
}
