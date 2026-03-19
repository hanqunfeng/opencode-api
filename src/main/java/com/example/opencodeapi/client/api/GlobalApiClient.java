package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.dto.HealthResponse;
import com.example.opencodeapi.client.http.OpencodeHttpClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GlobalApiClient {

    private final OpencodeHttpClient httpClient;

    public HealthResponse getHealth() {
        return httpClient.get("/global/health", HealthResponse.class);
    }

    public String getGlobalEvent() {
        return httpClient.readFirstSseEvent("/global/event");
    }
}
