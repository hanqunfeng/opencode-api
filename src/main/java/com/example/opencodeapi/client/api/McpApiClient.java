package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class McpApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode getStatus() {
        return httpClient.get("/mcp", JsonNode.class);
    }

    public JsonNode add(String name, Map<String, Object> mcpConfig) {
        Map<String, Object> body = Map.of("name", name, "config", mcpConfig);
        return httpClient.post("/mcp", body, JsonNode.class);
    }
}
