package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.VcsApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class VcsService {

    private final VcsApiClient vcsApiClient;

    public JsonNode get() {
        return vcsApiClient.get();
    }
}
