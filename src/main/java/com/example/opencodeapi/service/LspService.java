package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.LspApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class LspService {

    private final LspApiClient lspApiClient;

    public JsonNode getStatus() {
        return lspApiClient.getStatus();
    }
}
