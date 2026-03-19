package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.InstanceApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class InstanceService {

    private final InstanceApiClient instanceApiClient;

    public JsonNode dispose() {
        return instanceApiClient.dispose();
    }
}
