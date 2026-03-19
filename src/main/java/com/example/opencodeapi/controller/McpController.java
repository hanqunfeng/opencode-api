package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.McpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok(mcpService.getStatus()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JsonNode>> add(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> mcpConfig = (Map<String, Object>) body.get("config");
        return ResponseEntity.ok(ApiResponse.ok(mcpService.add(name, mcpConfig)));
    }
}
