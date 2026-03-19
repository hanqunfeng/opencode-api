package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TuiApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode appendPrompt(String text) {
        return httpClient.post("/tui/append-prompt", Map.of("text", text), JsonNode.class);
    }

    public JsonNode openHelp() {
        return httpClient.post("/tui/open-help", null, JsonNode.class);
    }

    public JsonNode openSessions() {
        return httpClient.post("/tui/open-sessions", null, JsonNode.class);
    }

    public JsonNode openThemes() {
        return httpClient.post("/tui/open-themes", null, JsonNode.class);
    }

    public JsonNode openModels() {
        return httpClient.post("/tui/open-models", null, JsonNode.class);
    }

    public JsonNode submitPrompt() {
        return httpClient.post("/tui/submit-prompt", null, JsonNode.class);
    }

    public JsonNode clearPrompt() {
        return httpClient.post("/tui/clear-prompt", null, JsonNode.class);
    }

    public JsonNode executeCommand(String command) {
        return httpClient.post("/tui/execute-command", Map.of("command", command), JsonNode.class);
    }

    public JsonNode showToast(String title, String message, String variant) {
        Map<String, Object> body = new HashMap<>();
        if (title != null) body.put("title", title);
        body.put("message", message);
        body.put("variant", variant);
        return httpClient.post("/tui/show-toast", body, JsonNode.class);
    }

    public JsonNode controlNext() {
        return httpClient.getBlocking("/tui/control/next", JsonNode.class);
    }

    public JsonNode controlResponse(Map<String, Object> body) {
        return httpClient.post("/tui/control/response", body, JsonNode.class);
    }
}
