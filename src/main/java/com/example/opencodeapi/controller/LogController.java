package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping
    public ResponseEntity<ApiResponse<JsonNode>> write(@RequestBody Map<String, Object> body) {
        String service = (String) body.get("service");
        String level = (String) body.get("level");
        String message = (String) body.get("message");
        @SuppressWarnings("unchecked")
        Map<String, Object> extra = (Map<String, Object>) body.get("extra");
        return ResponseEntity.ok(ApiResponse.ok(logService.write(service, level, message, extra)));
    }
}
