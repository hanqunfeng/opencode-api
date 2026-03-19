package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class FileApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode find(String pattern) {
        return httpClient.get("/find", Map.of("pattern", pattern), JsonNode.class);
    }

    public JsonNode findFile(String query, String type, String directory, Integer limit, String dirs) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", query);
        if (type != null) params.put("type", type);
        if (directory != null) params.put("directory", directory);
        if (limit != null) params.put("limit", limit.toString());
        if (dirs != null) params.put("dirs", dirs);
        return httpClient.get("/find/file", params, JsonNode.class);
    }

    public JsonNode findSymbol(String query) {
        return httpClient.get("/find/symbol", Map.of("query", query), JsonNode.class);
    }

    public JsonNode list(String path) {
        return httpClient.get("/file", Map.of("path", path), JsonNode.class);
    }

    public JsonNode getContent(String path) {
        return httpClient.get("/file/content", Map.of("path", path), JsonNode.class);
    }

    public JsonNode getStatus() {
        return httpClient.get("/file/status", JsonNode.class);
    }
}
