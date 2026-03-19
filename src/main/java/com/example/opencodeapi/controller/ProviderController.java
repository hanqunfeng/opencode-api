package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> list() {
        return ResponseEntity.ok(ApiResponse.ok(providerService.list()));
    }

    @GetMapping("/auth")
    public ResponseEntity<ApiResponse<JsonNode>> getAuth() {
        return ResponseEntity.ok(ApiResponse.ok(providerService.getAuth()));
    }

    @PostMapping("/{providerId}/oauth/authorize")
    public ResponseEntity<ApiResponse<JsonNode>> oauthAuthorize(@PathVariable String providerId) {
        return ResponseEntity.ok(ApiResponse.ok(providerService.oauthAuthorize(providerId)));
    }

    @PostMapping("/{providerId}/oauth/callback")
    public ResponseEntity<ApiResponse<JsonNode>> oauthCallback(@PathVariable String providerId,
                                                                @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(providerService.oauthCallback(providerId, body)));
    }
}
