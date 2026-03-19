package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class AuthApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode set(String id, Map<String, Object> body) {
        return httpClient.put("/auth/" + id, body, JsonNode.class);
    }
}
