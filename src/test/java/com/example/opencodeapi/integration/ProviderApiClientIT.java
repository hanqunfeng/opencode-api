package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.ProviderApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProviderApiClientIT extends IntegrationTestBase {

    private static ProviderApiClient client;

    @BeforeAll
    static void setUp() {
        client = new ProviderApiClient(httpClient);
    }

    @Test
    void listProviders_returnsProviderInfo() {
        JsonNode result = client.list();
        assertNotNull(result);
    }

    @Test
    void getProviderAuth_returnsAuthMethods() {
        JsonNode result = client.getAuth();
        assertNotNull(result);
    }

    @Test
    void oauthAuthorize_callableWithDummyProvider() {
        try {
            client.oauthAuthorize("dummy-provider");
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400,
                    "Expected client/server error for non-existent provider");
        }
    }

    @Test
    void oauthCallback_callableWithDummyProvider() {
        try {
            client.oauthCallback("dummy-provider", Map.of());
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400,
                    "Expected client/server error for non-existent provider");
        }
    }
}
