package com.example.opencodeapi.integration;

import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;

public final class ApiAssertions {

    private ApiAssertions() {
    }

    public static void assertJsonHasField(JsonNode node, String fieldName) {
        assertNotNull(node, "Response should not be null");
        assertTrue(node.has(fieldName), "Response should contain field '%s', got: %s"
                .formatted(fieldName, node.toString().substring(0, Math.min(node.toString().length(), 200))));
    }

    public static void assertJsonArray(JsonNode node) {
        assertNotNull(node, "Response should not be null");
        assertTrue(node.isArray(), "Response should be an array, got: " + node.getNodeType());
    }

    public static void assertJsonBoolean(JsonNode node, boolean expected) {
        assertNotNull(node, "Response should not be null");
        assertTrue(node.isBoolean(), "Response should be a boolean");
        assertEquals(expected, node.asBoolean());
    }

    public static void assertSseEvent(String event) {
        assertNotNull(event, "SSE event should not be null");
        assertFalse(event.isBlank(), "SSE event should not be blank");
    }
}
