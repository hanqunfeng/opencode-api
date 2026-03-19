package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.AgentApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentApiClient agentApiClient;

    public JsonNode list() {
        return agentApiClient.list();
    }
}
