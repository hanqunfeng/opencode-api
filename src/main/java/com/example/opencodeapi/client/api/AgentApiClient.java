package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AgentApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/agent", JsonNode.class);
    }
}
