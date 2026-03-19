package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.TuiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tui")
@RequiredArgsConstructor
public class TuiController {

    private final TuiService tuiService;

    @PostMapping("/append-prompt")
    public ResponseEntity<ApiResponse<JsonNode>> appendPrompt(@RequestBody Map<String, Object> body) {
        String text = (String) body.get("text");
        return ResponseEntity.ok(ApiResponse.ok(tuiService.appendPrompt(text)));
    }

    @PostMapping("/open-help")
    public ResponseEntity<ApiResponse<JsonNode>> openHelp() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.openHelp()));
    }

    @PostMapping("/open-sessions")
    public ResponseEntity<ApiResponse<JsonNode>> openSessions() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.openSessions()));
    }

    @PostMapping("/open-themes")
    public ResponseEntity<ApiResponse<JsonNode>> openThemes() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.openThemes()));
    }

    @PostMapping("/open-models")
    public ResponseEntity<ApiResponse<JsonNode>> openModels() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.openModels()));
    }

    @PostMapping("/submit-prompt")
    public ResponseEntity<ApiResponse<JsonNode>> submitPrompt() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.submitPrompt()));
    }

    @PostMapping("/clear-prompt")
    public ResponseEntity<ApiResponse<JsonNode>> clearPrompt() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.clearPrompt()));
    }

    @PostMapping("/execute-command")
    public ResponseEntity<ApiResponse<JsonNode>> executeCommand(@RequestBody Map<String, Object> body) {
        String command = (String) body.get("command");
        return ResponseEntity.ok(ApiResponse.ok(tuiService.executeCommand(command)));
    }

    @PostMapping("/show-toast")
    public ResponseEntity<ApiResponse<JsonNode>> showToast(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String message = (String) body.get("message");
        String variant = (String) body.get("variant");
        return ResponseEntity.ok(ApiResponse.ok(tuiService.showToast(title, message, variant)));
    }

    @GetMapping("/control/next")
    public ResponseEntity<ApiResponse<JsonNode>> controlNext() {
        try {
            JsonNode result = tuiService.controlNext();
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (OpencodeConnectionException e) {
            log.debug("controlNext timed out, returning empty response");
            return ResponseEntity.ok(ApiResponse.ok(null));
        }
    }

    @PostMapping("/control/response")
    public ResponseEntity<ApiResponse<JsonNode>> controlResponse(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.controlResponse(body)));
    }
}
