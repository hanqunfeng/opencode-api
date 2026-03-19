package com.example.opencodeapi.config;

import com.example.opencodeapi.client.api.*;
import com.example.opencodeapi.client.config.OpencodeClientConfig;
import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.example.opencodeapi.service.SseProxyHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class OpencodeBeansConfig {

    @Value("${opencode.base-url:}")
    private String baseUrl;

    @Value("${opencode.server.username:opencode}")
    private String username;

    @Value("${opencode.server.password:}")
    private String password;

    @Value("${opencode.timeout-ms:10000}")
    private String timeoutMs;

    @Value("${opencode.blocking-timeout-ms:3000}")
    private String blockingTimeoutMs;

    @Value("${opencode.sse.timeout-ms:30000}")
    private long sseTimeoutMs;

    @Value("${opencode.sse.thread-pool-size:4}")
    private int sseThreadPoolSize;

    @Bean
    public OpencodeClientConfig opencodeClientConfig() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Property 'opencode.base-url' is required. "
                            + "Set via application.properties or OPENCODE_BASE_URL env variable. "
                            + "Example: http://127.0.0.1:4096");
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new IllegalStateException(
                    "Property 'opencode.base-url' must start with http:// or https://. Got: " + baseUrl);
        }
        log.info("Connecting to OpenCode Server at {} (lazy)", baseUrl);
        return new OpencodeClientConfig(baseUrl, username, password, timeoutMs, blockingTimeoutMs);
    }

    @Bean
    public OpencodeHttpClient opencodeHttpClient(OpencodeClientConfig config) {
        return new OpencodeHttpClient(config);
    }

    @Bean
    public ExecutorService sseExecutor() {
        return Executors.newFixedThreadPool(sseThreadPoolSize);
    }

    @Bean
    public SseProxyHelper sseProxyHelper(OpencodeClientConfig config, ExecutorService sseExecutor) {
        return new SseProxyHelper(config, sseExecutor, sseTimeoutMs);
    }

    // --- API Clients ---

    @Bean
    public GlobalApiClient globalApiClient(OpencodeHttpClient httpClient) {
        return new GlobalApiClient(httpClient);
    }

    @Bean
    public ProjectApiClient projectApiClient(OpencodeHttpClient httpClient) {
        return new ProjectApiClient(httpClient);
    }

    @Bean
    public PathApiClient pathApiClient(OpencodeHttpClient httpClient) {
        return new PathApiClient(httpClient);
    }

    @Bean
    public VcsApiClient vcsApiClient(OpencodeHttpClient httpClient) {
        return new VcsApiClient(httpClient);
    }

    @Bean
    public InstanceApiClient instanceApiClient(OpencodeHttpClient httpClient) {
        return new InstanceApiClient(httpClient);
    }

    @Bean
    public ConfigApiClient configApiClient(OpencodeHttpClient httpClient) {
        return new ConfigApiClient(httpClient);
    }

    @Bean
    public ProviderApiClient providerApiClient(OpencodeHttpClient httpClient) {
        return new ProviderApiClient(httpClient);
    }

    @Bean
    public SessionApiClient sessionApiClient(OpencodeHttpClient httpClient) {
        return new SessionApiClient(httpClient);
    }

    @Bean
    public MessageApiClient messageApiClient(OpencodeHttpClient httpClient) {
        return new MessageApiClient(httpClient);
    }

    @Bean
    public CommandApiClient commandApiClient(OpencodeHttpClient httpClient) {
        return new CommandApiClient(httpClient);
    }

    @Bean
    public FileApiClient fileApiClient(OpencodeHttpClient httpClient) {
        return new FileApiClient(httpClient);
    }

    @Bean
    public AgentApiClient agentApiClient(OpencodeHttpClient httpClient) {
        return new AgentApiClient(httpClient);
    }

    @Bean
    public LogApiClient logApiClient(OpencodeHttpClient httpClient) {
        return new LogApiClient(httpClient);
    }

    @Bean
    public AuthApiClient authApiClient(OpencodeHttpClient httpClient) {
        return new AuthApiClient(httpClient);
    }

    @Bean
    public EventApiClient eventApiClient(OpencodeHttpClient httpClient) {
        return new EventApiClient(httpClient);
    }

    @Bean
    public DocApiClient docApiClient(OpencodeHttpClient httpClient) {
        return new DocApiClient(httpClient);
    }

    @Bean
    public LspApiClient lspApiClient(OpencodeHttpClient httpClient) {
        return new LspApiClient(httpClient);
    }

    @Bean
    public FormatterApiClient formatterApiClient(OpencodeHttpClient httpClient) {
        return new FormatterApiClient(httpClient);
    }

    @Bean
    public McpApiClient mcpApiClient(OpencodeHttpClient httpClient) {
        return new McpApiClient(httpClient);
    }

    @Bean
    public TuiApiClient tuiApiClient(OpencodeHttpClient httpClient) {
        return new TuiApiClient(httpClient);
    }
}
