package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> get() {
        return ResponseEntity.ok(ApiResponse.ok(configService.get()));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<JsonNode>> patch(@RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(ApiResponse.ok(configService.patch(updates)));
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<JsonNode>> getProviders() {
        return ResponseEntity.ok(ApiResponse.ok(configService.getProviders()));
    }
}
