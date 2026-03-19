package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.config.OpencodeClientConfig;
import com.example.opencodeapi.client.dto.HealthResponse;
import com.example.opencodeapi.client.http.OpencodeHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@DisabledIfEnvironmentVariable(named = "SKIP_OPENCODE_IT", matches = "true",
        disabledReason = "Skipped: SKIP_OPENCODE_IT=true")
public abstract class IntegrationTestBase {

    protected static OpencodeClientConfig config;
    protected static OpencodeHttpClient httpClient;

    @BeforeAll
    static void initClient() {
        String baseUrl = System.getenv("OPENCODE_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://127.0.0.1:4096";
        }
        config = new OpencodeClientConfig(
                baseUrl,
                System.getenv("OPENCODE_USERNAME"),
                System.getenv("OPENCODE_PASSWORD"),
                System.getenv("OPENCODE_TIMEOUT_MS"),
                System.getenv("OPENCODE_BLOCKING_TIMEOUT_MS"));
        httpClient = new OpencodeHttpClient(config);

        HealthResponse health = httpClient.get("/global/health", HealthResponse.class);
        assertTrue(health.isHealthy(),
                "opencode server is not healthy. Please run: opencode serve --hostname 127.0.0.1 --port 4096");
    }
}
