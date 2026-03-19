package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/project", JsonNode.class);
    }

    public JsonNode getCurrent() {
        return httpClient.get("/project/current", JsonNode.class);
    }
}
