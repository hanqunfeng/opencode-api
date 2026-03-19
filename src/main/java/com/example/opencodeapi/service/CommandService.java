package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.CommandApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final CommandApiClient commandApiClient;

    public JsonNode list() {
        return commandApiClient.list();
    }
}
