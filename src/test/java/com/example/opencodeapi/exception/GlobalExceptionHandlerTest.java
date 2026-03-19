package com.example.opencodeapi.exception;

import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleApiException_transparentlyForwardsStatusCode() {
        var ex = new OpencodeApiException(404, "/session/abc", "not found");
        ResponseEntity<ApiResponse<?>> response = handler.handleApiException(ex);
        assertEquals(404, response.getStatusCode().value());
        assertEquals(404, response.getBody().getCode());
        assertNotNull(response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void handleConnectionException_returns502() {
        var ex = new OpencodeConnectionException("/health", new RuntimeException("timeout"));
        ResponseEntity<ApiResponse<?>> response = handler.handleConnectionException(ex);
        assertEquals(502, response.getStatusCode().value());
        assertEquals(502, response.getBody().getCode());
    }

    @Test
    void handleIllegalArgument_returns400() {
        var ex = new IllegalArgumentException("bad param");
        ResponseEntity<ApiResponse<?>> response = handler.handleIllegalArgument(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("bad param", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_returns500() {
        var ex = new RuntimeException("unexpected");
        ResponseEntity<ApiResponse<?>> response = handler.handleGenericException(ex);
        assertEquals(500, response.getStatusCode().value());
        assertEquals(500, response.getBody().getCode());
    }
}
