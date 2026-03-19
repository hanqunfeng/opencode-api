package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> list() {
        return ResponseEntity.ok(ApiResponse.ok(projectService.list()));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<JsonNode>> getCurrent() {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getCurrent()));
    }
}
