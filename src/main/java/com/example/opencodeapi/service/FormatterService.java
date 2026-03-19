package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.FormatterApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class FormatterService {

    private final FormatterApiClient formatterApiClient;

    public JsonNode getStatus() {
        return formatterApiClient.getStatus();
    }
}
