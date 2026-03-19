package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InstanceApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode dispose() {
        return httpClient.post("/instance/dispose", null, JsonNode.class);
    }
}
