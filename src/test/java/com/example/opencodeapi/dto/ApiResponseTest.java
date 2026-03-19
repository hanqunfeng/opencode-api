package com.example.opencodeapi.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void ok_wrapsDataWithCode200() {
        ApiResponse<String> response = ApiResponse.ok("hello");
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("hello", response.getData());
    }

    @Test
    void ok_withNullData() {
        ApiResponse<Void> response = ApiResponse.ok(null);
        assertEquals(200, response.getCode());
        assertNull(response.getData());
    }

    @Test
    void error_setsCodeAndMessage() {
        ApiResponse<?> response = ApiResponse.error(404, "Not Found");
        assertEquals(404, response.getCode());
        assertEquals("Not Found", response.getMessage());
        assertNull(response.getData());
    }
}
