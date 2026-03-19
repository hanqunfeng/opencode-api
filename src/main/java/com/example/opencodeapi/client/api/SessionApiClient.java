package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class SessionApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/session", JsonNode.class);
    }

    public JsonNode create(String parentId, String title) {
        Map<String, Object> body = new HashMap<>();
        if (parentId != null) body.put("parentID", parentId);
        if (title != null) body.put("title", title);
        return httpClient.post("/session", body, JsonNode.class);
    }

    public JsonNode getStatus() {
        return httpClient.get("/session/status", JsonNode.class);
    }

    public JsonNode get(String id) {
        return httpClient.get("/session/" + id, JsonNode.class);
    }

    public JsonNode delete(String id) {
        return httpClient.delete("/session/" + id, JsonNode.class);
    }

    public JsonNode patch(String id, Map<String, Object> body) {
        return httpClient.patch("/session/" + id, body, JsonNode.class);
    }

    public JsonNode getChildren(String id) {
        return httpClient.get("/session/" + id + "/children", JsonNode.class);
    }

    public JsonNode getTodo(String id) {
        return httpClient.get("/session/" + id + "/todo", JsonNode.class);
    }

    public JsonNode init(String id, Map<String, Object> body) {
        return httpClient.post("/session/" + id + "/init", body, JsonNode.class);
    }

    public JsonNode fork(String id, String messageId) {
        Map<String, Object> body = new HashMap<>();
        if (messageId != null) body.put("messageID", messageId);
        return httpClient.post("/session/" + id + "/fork", body, JsonNode.class);
    }

    public JsonNode abort(String id) {
        return httpClient.post("/session/" + id + "/abort", null, JsonNode.class);
    }

    public JsonNode share(String id) {
        return httpClient.post("/session/" + id + "/share", null, JsonNode.class);
    }

    public JsonNode unshare(String id) {
        return httpClient.delete("/session/" + id + "/share", JsonNode.class);
    }

    public JsonNode getDiff(String id, String messageId) {
        Map<String, String> params = messageId != null ? Map.of("messageID", messageId) : null;
        return httpClient.get("/session/" + id + "/diff", params, JsonNode.class);
    }

    public JsonNode summarize(String id, Map<String, Object> body) {
        return httpClient.post("/session/" + id + "/summarize", body, JsonNode.class);
    }

    public JsonNode revert(String id, Map<String, Object> body) {
        return httpClient.post("/session/" + id + "/revert", body, JsonNode.class);
    }

    public JsonNode unrevert(String id) {
        return httpClient.post("/session/" + id + "/unrevert", null, JsonNode.class);
    }

    public JsonNode respondToPermission(String id, String permissionId, Map<String, Object> body) {
        return httpClient.post("/session/" + id + "/permissions/" + permissionId, body, JsonNode.class);
    }
}
