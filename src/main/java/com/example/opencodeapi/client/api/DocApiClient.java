package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocApiClient {

    private final OpencodeHttpClient httpClient;

    public String get() {
        return httpClient.getHtml("/doc");
    }
}
