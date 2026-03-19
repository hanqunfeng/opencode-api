package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.McpApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class McpService {

    private final McpApiClient mcpApiClient;

    public JsonNode getStatus() {
        return mcpApiClient.getStatus();
    }

    public JsonNode add(String name, Map<String, Object> mcpConfig) {
        return mcpApiClient.add(name, mcpConfig);
    }
}
