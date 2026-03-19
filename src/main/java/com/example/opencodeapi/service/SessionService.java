package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.SessionApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionApiClient sessionApiClient;

    public JsonNode list() {
        return sessionApiClient.list();
    }

    public JsonNode create(String parentId, String title) {
        return sessionApiClient.create(parentId, title);
    }

    public JsonNode getStatus() {
        return sessionApiClient.getStatus();
    }

    public JsonNode get(String id) {
        return sessionApiClient.get(id);
    }

    public JsonNode delete(String id) {
        return sessionApiClient.delete(id);
    }

    public JsonNode patch(String id, Map<String, Object> body) {
        return sessionApiClient.patch(id, body);
    }

    public JsonNode getChildren(String id) {
        return sessionApiClient.getChildren(id);
    }

    public JsonNode getTodo(String id) {
        return sessionApiClient.getTodo(id);
    }

    public JsonNode init(String id, Map<String, Object> body) {
        return sessionApiClient.init(id, body);
    }

    public JsonNode fork(String id, String messageId) {
        return sessionApiClient.fork(id, messageId);
    }

    public JsonNode abort(String id) {
        return sessionApiClient.abort(id);
    }

    public JsonNode share(String id) {
        return sessionApiClient.share(id);
    }

    public JsonNode unshare(String id) {
        return sessionApiClient.unshare(id);
    }

    public JsonNode getDiff(String id, String messageId) {
        return sessionApiClient.getDiff(id, messageId);
    }

    public JsonNode summarize(String id, Map<String, Object> body) {
        return sessionApiClient.summarize(id, body);
    }

    public JsonNode revert(String id, Map<String, Object> body) {
        return sessionApiClient.revert(id, body);
    }

    public JsonNode unrevert(String id) {
        return sessionApiClient.unrevert(id);
    }

    public JsonNode respondToPermission(String id, String permissionId, Map<String, Object> body) {
        return sessionApiClient.respondToPermission(id, permissionId, body);
    }
}
