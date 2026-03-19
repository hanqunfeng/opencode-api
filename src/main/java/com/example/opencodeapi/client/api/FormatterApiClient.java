package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormatterApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode getStatus() {
        return httpClient.get("/formatter", JsonNode.class);
    }
}
