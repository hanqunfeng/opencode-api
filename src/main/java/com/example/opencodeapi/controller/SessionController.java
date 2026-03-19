package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> list() {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.list()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JsonNode>> create(@RequestBody(required = false) Map<String, Object> body) {
        String parentId = body != null ? (String) body.get("parentID") : null;
        String title = body != null ? (String) body.get("title") : null;
        return ResponseEntity.ok(ApiResponse.ok(sessionService.create(parentId, title)));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<JsonNode>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getStatus()));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<JsonNode>> get(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.get(sessionId)));
    }

    @PatchMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<JsonNode>> patch(@PathVariable String sessionId,
                                                       @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.patch(sessionId, body)));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<JsonNode>> delete(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.delete(sessionId)));
    }

    @GetMapping("/{sessionId}/children")
    public ResponseEntity<ApiResponse<JsonNode>> getChildren(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getChildren(sessionId)));
    }

    @GetMapping("/{sessionId}/todo")
    public ResponseEntity<ApiResponse<JsonNode>> getTodo(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getTodo(sessionId)));
    }

    @PostMapping("/{sessionId}/init")
    public ResponseEntity<ApiResponse<JsonNode>> init(@PathVariable String sessionId,
                                                      @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.init(sessionId, body)));
    }

    @PostMapping("/{sessionId}/fork")
    public ResponseEntity<ApiResponse<JsonNode>> fork(@PathVariable String sessionId,
                                                      @RequestBody(required = false) Map<String, Object> body) {
        String messageId = body != null ? (String) body.get("messageID") : null;
        return ResponseEntity.ok(ApiResponse.ok(sessionService.fork(sessionId, messageId)));
    }

    @PostMapping("/{sessionId}/abort")
    public ResponseEntity<ApiResponse<JsonNode>> abort(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.abort(sessionId)));
    }

    @PostMapping("/{sessionId}/share")
    public ResponseEntity<ApiResponse<JsonNode>> share(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.share(sessionId)));
    }

    @DeleteMapping("/{sessionId}/share")
    public ResponseEntity<ApiResponse<JsonNode>> unshare(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.unshare(sessionId)));
    }

    @GetMapping("/{sessionId}/diff")
    public ResponseEntity<ApiResponse<JsonNode>> getDiff(@PathVariable String sessionId,
                                                         @RequestParam(required = false) String messageID) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.getDiff(sessionId, messageID)));
    }

    @PostMapping("/{sessionId}/summarize")
    public ResponseEntity<ApiResponse<JsonNode>> summarize(@PathVariable String sessionId,
                                                           @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.summarize(sessionId, body)));
    }

    @PostMapping("/{sessionId}/revert")
    public ResponseEntity<ApiResponse<JsonNode>> revert(@PathVariable String sessionId,
                                                        @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.revert(sessionId, body)));
    }

    @PostMapping("/{sessionId}/unrevert")
    public ResponseEntity<ApiResponse<JsonNode>> unrevert(@PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(sessionService.unrevert(sessionId)));
    }

    @PostMapping("/{sessionId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<JsonNode>> respondToPermission(@PathVariable String sessionId,
                                                                     @PathVariable String permissionId,
                                                                     @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(
                sessionService.respondToPermission(sessionId, permissionId, body)));
    }
}
