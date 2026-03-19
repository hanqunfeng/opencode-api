package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ProviderApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/provider", JsonNode.class);
    }

    public JsonNode getAuth() {
        return httpClient.get("/provider/auth", JsonNode.class);
    }

    public JsonNode oauthAuthorize(String providerId) {
        return httpClient.post("/provider/" + providerId + "/oauth/authorize", null, JsonNode.class);
    }

    public JsonNode oauthCallback(String providerId, Map<String, Object> body) {
        return httpClient.post("/provider/" + providerId + "/oauth/callback", body, JsonNode.class);
    }
}
