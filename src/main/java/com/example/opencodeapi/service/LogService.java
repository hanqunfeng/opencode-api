package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.LogApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogApiClient logApiClient;

    public JsonNode write(String service, String level, String message, Map<String, Object> extra) {
        return logApiClient.write(service, level, message, extra);
    }
}
