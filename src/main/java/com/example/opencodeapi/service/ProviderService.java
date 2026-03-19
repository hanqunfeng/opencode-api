package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.ProviderApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderApiClient providerApiClient;

    public JsonNode list() {
        return providerApiClient.list();
    }

    public JsonNode getAuth() {
        return providerApiClient.getAuth();
    }

    public JsonNode oauthAuthorize(String providerId) {
        return providerApiClient.oauthAuthorize(providerId);
    }

    public JsonNode oauthCallback(String providerId, Map<String, Object> body) {
        return providerApiClient.oauthCallback(providerId, body);
    }
}
