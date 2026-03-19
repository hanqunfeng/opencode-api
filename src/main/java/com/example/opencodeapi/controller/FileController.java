package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/api/v1/find")
    public ResponseEntity<ApiResponse<JsonNode>> find(@RequestParam String pattern) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.find(pattern)));
    }

    @GetMapping("/api/v1/find/file")
    public ResponseEntity<ApiResponse<JsonNode>> findFile(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String dirs) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.findFile(query, type, directory, limit, dirs)));
    }

    @GetMapping("/api/v1/find/symbol")
    public ResponseEntity<ApiResponse<JsonNode>> findSymbol(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.findSymbol(query)));
    }

    @GetMapping("/api/v1/file")
    public ResponseEntity<ApiResponse<JsonNode>> list(@RequestParam String path) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.list(path)));
    }

    @GetMapping("/api/v1/file/content")
    public ResponseEntity<ApiResponse<JsonNode>> getContent(@RequestParam String path) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.getContent(path)));
    }

    @GetMapping("/api/v1/file/status")
    public ResponseEntity<ApiResponse<JsonNode>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok(fileService.getStatus()));
    }
}
