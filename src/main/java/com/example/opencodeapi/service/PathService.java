package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.PathApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class PathService {

    private final PathApiClient pathApiClient;

    public JsonNode get() {
        return pathApiClient.get();
    }
}
