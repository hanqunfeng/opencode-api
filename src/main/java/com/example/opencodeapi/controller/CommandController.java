package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.CommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/command")
@RequiredArgsConstructor
public class CommandController {

    private final CommandService commandService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> list() {
        return ResponseEntity.ok(ApiResponse.ok(commandService.list()));
    }
}
