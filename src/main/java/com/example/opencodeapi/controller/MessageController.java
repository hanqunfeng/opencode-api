package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/session/{sessionId}")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/message")
    public ResponseEntity<ApiResponse<JsonNode>> list(@PathVariable String sessionId,
                                                      @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.list(sessionId, limit)));
    }

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<JsonNode>> send(@PathVariable String sessionId,
                                                      @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.send(sessionId, body)));
    }

    @GetMapping("/message/{messageId}")
    public ResponseEntity<ApiResponse<JsonNode>> get(@PathVariable String sessionId,
                                                     @PathVariable String messageId) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.get(sessionId, messageId)));
    }

    @PostMapping("/prompt_async")
    public ResponseEntity<Void> promptAsync(@PathVariable String sessionId,
                                            @RequestBody Map<String, Object> body) {
        messageService.promptAsync(sessionId, body);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/command")
    public ResponseEntity<ApiResponse<JsonNode>> executeCommand(@PathVariable String sessionId,
                                                                @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.executeCommand(sessionId, body)));
    }

    @PostMapping("/shell")
    public ResponseEntity<ApiResponse<JsonNode>> executeShell(@PathVariable String sessionId,
                                                              @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.executeShell(sessionId, body)));
    }
}
