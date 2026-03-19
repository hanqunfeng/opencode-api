package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PathApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode get() {
        return httpClient.get("/path", JsonNode.class);
    }
}
