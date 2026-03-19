package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.AuthApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthApiClient authApiClient;

    public JsonNode set(String id, Map<String, Object> body) {
        return authApiClient.set(id, body);
    }
}
