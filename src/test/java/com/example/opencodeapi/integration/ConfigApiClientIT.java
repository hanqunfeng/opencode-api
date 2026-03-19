package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.ConfigApiClient;
import com.example.opencodeapi.client.api.InstanceApiClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigApiClientIT extends IntegrationTestBase {

    private static ConfigApiClient configClient;
    private static InstanceApiClient instanceClient;

    @BeforeAll
    static void setUp() {
        configClient = new ConfigApiClient(httpClient);
        instanceClient = new InstanceApiClient(httpClient);
    }

    @Test
    void getConfig_returnsConfig() {
        JsonNode config = configClient.get();
        assertNotNull(config);
        System.out.println(config);
    }

    @Test
    void patchConfig_returnsUpdatedConfig() {
        JsonNode result = configClient.patch(java.util.Map.of());
        assertNotNull(result);
    }

    @Test
    void getConfigProviders_returnsProviders() {
        JsonNode providers = configClient.getProviders();
        assertNotNull(providers);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_DESTRUCTIVE_IT", matches = "true")
    void dispose_destroysInstance() {
        JsonNode result = instanceClient.dispose();
        assertNotNull(result);
    }
}
