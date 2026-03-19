package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.ConfigApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigApiClient configApiClient;

    public JsonNode get() {
        return configApiClient.get();
    }

    public JsonNode patch(Map<String, Object> updates) {
        return configApiClient.patch(updates);
    }

    public JsonNode getProviders() {
        return configApiClient.getProviders();
    }
}
