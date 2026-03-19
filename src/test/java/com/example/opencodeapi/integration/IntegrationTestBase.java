package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.config.OpencodeClientConfig;
import com.example.opencodeapi.client.dto.HealthResponse;
import com.example.opencodeapi.client.http.OpencodeHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@DisabledIfEnvironmentVariable(named = "SKIP_OPENCODE_IT", matches = "true",
        disabledReason = "Skipped: SKIP_OPENCODE_IT=true")
public abstract class IntegrationTestBase {

    protected static OpencodeClientConfig config;
    protected static OpencodeHttpClient httpClient;

    @BeforeAll
    static void initClient() {
        Properties dotenv = loadDotenv();

        String baseUrl = env("OPENCODE_BASE_URL", dotenv);
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://127.0.0.1:4096";
        }
        config = new OpencodeClientConfig(
                baseUrl,
                env("OPENCODE_SERVER_USERNAME", dotenv),
                env("OPENCODE_SERVER_PASSWORD", dotenv),
                env("OPENCODE_TIMEOUT_MS", dotenv),
                env("OPENCODE_BLOCKING_TIMEOUT_MS", dotenv));
        httpClient = new OpencodeHttpClient(config);

        HealthResponse health = httpClient.get("/global/health", HealthResponse.class);
        assertTrue(health.isHealthy(),
                "opencode server is not healthy. Please run: opencode serve --hostname 127.0.0.1 --port 4096");
    }

    private static String env(String key, Properties dotenv) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return dotenv.getProperty(key);
    }

    private static Properties loadDotenv() {
        Properties props = new Properties();
        Path dotenvPath = Path.of(".env");
        if (!Files.exists(dotenvPath)) {
            dotenvPath = Path.of(System.getProperty("user.dir"), ".env");
        }
        if (Files.exists(dotenvPath)) {
            try (var reader = Files.newBufferedReader(dotenvPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String k = line.substring(0, eq).trim();
                        String v = line.substring(eq + 1).trim();
                        if (v.length() >= 2 && ((v.startsWith("\"") && v.endsWith("\""))
                                || (v.startsWith("'") && v.endsWith("'")))) {
                            v = v.substring(1, v.length() - 1);
                        }
                        props.setProperty(k, v);
                    }
                }
            } catch (IOException e) {
                // .env read failure is non-fatal
            }
        }
        return props;
    }
}
