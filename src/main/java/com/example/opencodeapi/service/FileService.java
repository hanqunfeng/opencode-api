package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.FileApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileApiClient fileApiClient;

    public JsonNode find(String pattern) {
        return fileApiClient.find(pattern);
    }

    public JsonNode findFile(String query, String type, String directory, Integer limit, String dirs) {
        return fileApiClient.findFile(query, type, directory, limit, dirs);
    }

    public JsonNode findSymbol(String query) {
        return fileApiClient.findSymbol(query);
    }

    public JsonNode list(String path) {
        return fileApiClient.list(path);
    }

    public JsonNode getContent(String path) {
        return fileApiClient.getContent(path);
    }

    public JsonNode getStatus() {
        return fileApiClient.getStatus();
    }
}
