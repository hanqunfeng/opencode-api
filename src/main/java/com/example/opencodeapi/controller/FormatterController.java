package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.FormatterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/formatter")
@RequiredArgsConstructor
public class FormatterController {

    private final FormatterService formatterService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok(formatterService.getStatus()));
    }
}
