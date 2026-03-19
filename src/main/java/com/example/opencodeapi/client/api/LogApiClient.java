package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class LogApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode write(String service, String level, String message, Map<String, Object> extra) {
        Map<String, Object> body = new HashMap<>();
        body.put("service", service);
        body.put("level", level);
        body.put("message", message);
        if (extra != null) body.put("extra", extra);
        return httpClient.post("/log", body, JsonNode.class);
    }
}
