package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ConfigApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode get() {
        return httpClient.get("/config", JsonNode.class);
    }

    public JsonNode patch(Map<String, Object> updates) {
        return httpClient.patch("/config", updates, JsonNode.class);
    }

    public JsonNode getProviders() {
        return httpClient.get("/config/providers", JsonNode.class);
    }
}
