package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.MessageApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageApiClient messageApiClient;

    public JsonNode list(String sessionId, Integer limit) {
        return messageApiClient.list(sessionId, limit);
    }

    public JsonNode send(String sessionId, Map<String, Object> body) {
        return messageApiClient.send(sessionId, body);
    }

    public JsonNode get(String sessionId, String messageId) {
        return messageApiClient.get(sessionId, messageId);
    }

    public void promptAsync(String sessionId, Map<String, Object> body) {
        messageApiClient.promptAsync(sessionId, body);
    }

    public JsonNode executeCommand(String sessionId, Map<String, Object> body) {
        return messageApiClient.executeCommand(sessionId, body);
    }

    public JsonNode executeShell(String sessionId, Map<String, Object> body) {
        return messageApiClient.executeShell(sessionId, body);
    }
}
