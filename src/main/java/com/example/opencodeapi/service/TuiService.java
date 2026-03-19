package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.TuiApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TuiService {

    private final TuiApiClient tuiApiClient;

    public JsonNode appendPrompt(String text) {
        return tuiApiClient.appendPrompt(text);
    }

    public JsonNode openHelp() {
        return tuiApiClient.openHelp();
    }

    public JsonNode openSessions() {
        return tuiApiClient.openSessions();
    }

    public JsonNode openThemes() {
        return tuiApiClient.openThemes();
    }

    public JsonNode openModels() {
        return tuiApiClient.openModels();
    }

    public JsonNode submitPrompt() {
        return tuiApiClient.submitPrompt();
    }

    public JsonNode clearPrompt() {
        return tuiApiClient.clearPrompt();
    }

    public JsonNode executeCommand(String command) {
        return tuiApiClient.executeCommand(command);
    }

    public JsonNode showToast(String title, String message, String variant) {
        return tuiApiClient.showToast(title, message, variant);
    }

    public JsonNode controlNext() {
        return tuiApiClient.controlNext();
    }

    public JsonNode controlResponse(Map<String, Object> body) {
        return tuiApiClient.controlResponse(body);
    }
}
