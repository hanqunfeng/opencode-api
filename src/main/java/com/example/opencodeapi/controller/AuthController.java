package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JsonNode>> set(@PathVariable String id,
                                                     @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(authService.set(id, body)));
    }
}
