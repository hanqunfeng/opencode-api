package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.ProjectApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectApiClient projectApiClient;

    public JsonNode list() {
        return projectApiClient.list();
    }

    public JsonNode getCurrent() {
        return projectApiClient.getCurrent();
    }
}
