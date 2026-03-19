package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventApiClient {

    private final OpencodeHttpClient httpClient;

    public String getEvent() {
        return httpClient.readFirstSseEvent("/event");
    }
}
