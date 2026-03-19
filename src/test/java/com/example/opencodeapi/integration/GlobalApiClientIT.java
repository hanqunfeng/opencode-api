package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.GlobalApiClient;
import com.example.opencodeapi.client.dto.HealthResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

class GlobalApiClientIT extends IntegrationTestBase {

    private static GlobalApiClient client;

    @BeforeAll
    static void setUp() {
        client = new GlobalApiClient(httpClient);
    }

    @Test
    void getHealth_returnsHealthyStatus() {
        HealthResponse health = client.getHealth();
        assertTrue(health.isHealthy());
        assertNotNull(health.getVersion());
        assertFalse(health.getVersion().isBlank());
    }

    @Test
    void getGlobalEvent_receivesFirstSseEvent() {
        String event = client.getGlobalEvent();
        assertSseEvent(event);
    }
}
