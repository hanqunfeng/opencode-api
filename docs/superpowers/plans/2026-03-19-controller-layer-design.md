# Controller Layer 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 OpenCode API 增加 Controller 层（含 Service、DTO、异常处理、SSE 代理、配置），将其从纯客户端库转变为代理网关，对外暴露 `/api/v1/*` REST API，内部通过已有 API Client 转发到 OpenCode Server。

**Architecture:** Controller → Service → API Client → OpencodeHttpClient → OpenCode Server。统一 `ApiResponse<T>` 包装、`@RestControllerAdvice` 全局异常处理、`SseEmitter` SSE 代理转发、`@Configuration` Bean 注册。

**Tech Stack:** Java 21, Spring Boot 4.0.3, Spring WebMVC, Jackson 3.x (`tools.jackson`), Lombok, JUnit 5, MockMvc, `@MockitoBean`

**Spec:** `docs/superpowers/specs/2026-03-19-controller-layer-design.md`

---

## File Structure

> 路径缩写：`src/main/java/com/example/opencodeapi` → `main/`，`src/test/java/com/example/opencodeapi` → `test/`

### Source Files (to create — 44 files)

| File | Responsibility |
|------|---------------|
| `main/dto/ApiResponse.java` | 统一响应包装 |
| `main/exception/GlobalExceptionHandler.java` | 全局异常处理 |
| `main/config/OpencodeBeansConfig.java` | Spring Bean 配置（注册 Config、HttpClient、API Client、SSE 线程池、SseProxyHelper） |
| `main/service/SseProxyHelper.java` | SSE 代理工具类（连接上游、解析事件、转发 SseEmitter） |
| `main/service/GlobalService.java` | health + SSE 事件流 |
| `main/service/ProjectService.java` | 项目列表 + 当前项目 |
| `main/service/PathService.java` | 获取路径 |
| `main/service/VcsService.java` | VCS 信息 |
| `main/service/InstanceService.java` | 销毁实例 |
| `main/service/ConfigService.java` | 配置 CRUD |
| `main/service/ProviderService.java` | Provider + OAuth |
| `main/service/SessionService.java` | Session 全生命周期（18 个方法） |
| `main/service/MessageService.java` | 消息 + prompt + command + shell |
| `main/service/CommandService.java` | 命令列表 |
| `main/service/FileService.java` | 文件查找/列表/内容/状态 |
| `main/service/AgentService.java` | Agent 列表 |
| `main/service/LogService.java` | 写入日志 |
| `main/service/AuthService.java` | 设置认证 |
| `main/service/EventService.java` | 事件流 SSE |
| `main/service/DocService.java` | 文档 HTML |
| `main/service/LspService.java` | LSP 状态 |
| `main/service/FormatterService.java` | Formatter 状态 |
| `main/service/McpService.java` | MCP 状态 + 添加 |
| `main/service/TuiService.java` | TUI 操作（11 个方法） |
| `main/controller/GlobalController.java` | `/api/v1/global` |
| `main/controller/ProjectController.java` | `/api/v1/project` |
| `main/controller/PathController.java` | `/api/v1/path` |
| `main/controller/VcsController.java` | `/api/v1/vcs` |
| `main/controller/InstanceController.java` | `/api/v1/instance` |
| `main/controller/ConfigController.java` | `/api/v1/config` |
| `main/controller/ProviderController.java` | `/api/v1/provider` |
| `main/controller/SessionController.java` | `/api/v1/session` |
| `main/controller/MessageController.java` | `/api/v1/session/{sessionId}/message` + `/api/v1/session/{sessionId}` |
| `main/controller/CommandController.java` | `/api/v1/command` |
| `main/controller/FileController.java` | `/api/v1/find/*` + `/api/v1/file/*`（无类级别 @RequestMapping） |
| `main/controller/AgentController.java` | `/api/v1/agent` |
| `main/controller/LogController.java` | `/api/v1/log` |
| `main/controller/AuthController.java` | `/api/v1/auth` |
| `main/controller/EventController.java` | `/api/v1/event` |
| `main/controller/DocController.java` | `/api/v1/doc` |
| `main/controller/LspController.java` | `/api/v1/lsp` |
| `main/controller/FormatterController.java` | `/api/v1/formatter` |
| `main/controller/McpController.java` | `/api/v1/mcp` |
| `main/controller/TuiController.java` | `/api/v1/tui` |

### Test Files (to create — 22 files)

| File | Responsibility |
|------|---------------|
| `test/dto/ApiResponseTest.java` | ApiResponse 单元测试 |
| `test/exception/GlobalExceptionHandlerTest.java` | 异常处理单元测试 |
| `test/controller/GlobalControllerTest.java` | MockMvc 单元测试 |
| `test/controller/ProjectControllerTest.java` | MockMvc 单元测试 |
| `test/controller/PathControllerTest.java` | MockMvc 单元测试 |
| `test/controller/VcsControllerTest.java` | MockMvc 单元测试 |
| `test/controller/InstanceControllerTest.java` | MockMvc 单元测试 |
| `test/controller/ConfigControllerTest.java` | MockMvc 单元测试 |
| `test/controller/ProviderControllerTest.java` | MockMvc 单元测试 |
| `test/controller/SessionControllerTest.java` | MockMvc 单元测试 |
| `test/controller/MessageControllerTest.java` | MockMvc 单元测试 |
| `test/controller/CommandControllerTest.java` | MockMvc 单元测试 |
| `test/controller/FileControllerTest.java` | MockMvc 单元测试 |
| `test/controller/AgentControllerTest.java` | MockMvc 单元测试 |
| `test/controller/LogControllerTest.java` | MockMvc 单元测试 |
| `test/controller/AuthControllerTest.java` | MockMvc 单元测试 |
| `test/controller/EventControllerTest.java` | MockMvc 单元测试 |
| `test/controller/DocControllerTest.java` | MockMvc 单元测试 |
| `test/controller/LspControllerTest.java` | MockMvc 单元测试 |
| `test/controller/FormatterControllerTest.java` | MockMvc 单元测试 |
| `test/controller/McpControllerTest.java` | MockMvc 单元测试 |
| `test/controller/TuiControllerTest.java` | MockMvc 单元测试 |

### Files to Modify

| File | Change |
|------|--------|
| `src/main/resources/application.properties` | 添加 `opencode.*` 配置属性 |

**总计：新建 66 个文件，修改 1 个文件。**

> 注：集成测试（`*ControllerIT.java`）在当前计划中不包含——它们依赖运行中的 OpenCode Server，属于后续独立任务。

---

## Task 1: ApiResponse DTO

**Files:**
- Create: `src/main/java/com/example/opencodeapi/dto/ApiResponse.java`
- Create: `src/test/java/com/example/opencodeapi/dto/ApiResponseTest.java`

- [ ] **Step 1: Write ApiResponse unit test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=ApiResponseTest -pl . -q 2>&1 | tail -5`
Expected: FAIL — `ApiResponse` class does not exist

- [ ] **Step 3: Create ApiResponse**

```java
package com.example.opencodeapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=ApiResponseTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/dto/ApiResponse.java \
        src/test/java/com/example/opencodeapi/dto/ApiResponseTest.java
git commit -m "feat: add ApiResponse unified response wrapper"
```

---

## Task 2: GlobalExceptionHandler + Test

**Files:**
- Create: `src/main/java/com/example/opencodeapi/exception/GlobalExceptionHandler.java`
- Create: `src/test/java/com/example/opencodeapi/exception/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Write GlobalExceptionHandler test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=GlobalExceptionHandlerTest -pl . -q 2>&1 | tail -5`
Expected: FAIL — class does not exist

- [ ] **Step 3: Create GlobalExceptionHandler**

```java
package com.example.opencodeapi.exception;

import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OpencodeApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(OpencodeApiException ex) {
        log.warn("API error: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.error(ex.getStatusCode(), ex.getMessage()));
    }

    @ExceptionHandler(OpencodeConnectionException.class)
    public ResponseEntity<ApiResponse<?>> handleConnectionException(OpencodeConnectionException ex) {
        log.error("Connection error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(502, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error"));
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=GlobalExceptionHandlerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/exception/GlobalExceptionHandler.java \
        src/test/java/com/example/opencodeapi/exception/GlobalExceptionHandlerTest.java
git commit -m "feat: add GlobalExceptionHandler with unified error response"
```

---

## Task 3: OpencodeBeansConfig + application.properties

**Files:**
- Create: `src/main/java/com/example/opencodeapi/config/OpencodeBeansConfig.java`
- Modify: `src/main/resources/application.properties`

- [ ] **Step 1: Update application.properties**

将现有内容替换为：

```properties
spring.application.name=opencode-api

# OpenCode Server connection
opencode.base-url=${OPENCODE_BASE_URL:}
opencode.server.username=${OPENCODE_SERVER_USERNAME:opencode}
opencode.server.password=${OPENCODE_SERVER_PASSWORD:}
opencode.timeout-ms=${OPENCODE_TIMEOUT_MS:10000}
opencode.blocking-timeout-ms=${OPENCODE_BLOCKING_TIMEOUT_MS:3000}

# SSE proxy
opencode.sse.timeout-ms=${OPENCODE_SSE_TIMEOUT_MS:30000}
opencode.sse.thread-pool-size=${OPENCODE_SSE_THREAD_POOL_SIZE:4}
```

- [ ] **Step 2: Create OpencodeBeansConfig**

```java
package com.example.opencodeapi.config;

import com.example.opencodeapi.client.api.*;
import com.example.opencodeapi.client.config.OpencodeClientConfig;
import com.example.opencodeapi.client.http.OpencodeHttpClient;
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
```

- [ ] **Step 3: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/opencodeapi/config/OpencodeBeansConfig.java \
        src/main/resources/application.properties
git commit -m "feat: add OpencodeBeansConfig and Spring property configuration"
```

---

## Task 4: SseProxyHelper

**Files:**
- Create: `src/main/java/com/example/opencodeapi/service/SseProxyHelper.java`

- [ ] **Step 1: Create SseProxyHelper**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.config.OpencodeClientConfig;
import com.example.opencodeapi.client.http.OpencodeApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SseProxyHelper {

    private final OpencodeClientConfig config;
    private final ExecutorService sseExecutor;
    private final long defaultTimeoutMs;

    public SseProxyHelper(OpencodeClientConfig config, ExecutorService sseExecutor, long defaultTimeoutMs) {
        this.config = config;
        this.sseExecutor = sseExecutor;
        this.defaultTimeoutMs = defaultTimeoutMs;
    }

    public long getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    public long resolveTimeout(Long timeout) {
        if (timeout == null) {
            return defaultTimeoutMs;
        }
        if (timeout < 1000 || timeout > 300_000) {
            log.warn("SSE timeout {} ms out of range [1000, 300000], using default {} ms",
                    timeout, defaultTimeoutMs);
            return defaultTimeoutMs;
        }
        return timeout;
    }

    public SseEmitter proxy(String upstreamPath, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        AtomicReference<HttpURLConnection> connRef = new AtomicReference<>();

        Runnable cleanup = () -> {
            HttpURLConnection c = connRef.getAndSet(null);
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ignored) {
                }
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        sseExecutor.submit(() -> {
            try {
                HttpURLConnection conn = openSseConnection(upstreamPath);
                connRef.set(conn);

                int status = conn.getResponseCode();
                if (status != 200) {
                    emitter.completeWithError(
                            new OpencodeApiException(status, upstreamPath,
                                    "SSE connection failed with status " + status));
                    return;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    streamEvents(reader, emitter, upstreamPath);
                }
                emitter.complete();
            } catch (Exception e) {
                if (connRef.get() != null) {
                    log.warn("SSE proxy error for {}: {}", upstreamPath, e.getMessage());
                    try {
                        emitter.completeWithError(e);
                    } catch (Exception ignored) {
                    }
                }
            } finally {
                cleanup.run();
            }
        });

        return emitter;
    }

    private void streamEvents(BufferedReader reader, SseEmitter emitter,
                              String upstreamPath) throws Exception {
        StringBuilder dataBuilder = new StringBuilder();
        String eventName = null;
        String eventId = null;
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (!dataBuilder.isEmpty()) {
                    SseEmitter.SseEventBuilder builder = SseEmitter.event()
                            .data(dataBuilder.toString());
                    if (eventName != null) builder.name(eventName);
                    if (eventId != null) builder.id(eventId);
                    emitter.send(builder);
                    dataBuilder.setLength(0);
                    eventName = null;
                    eventId = null;
                }
            } else if (line.startsWith("data:")) {
                if (!dataBuilder.isEmpty()) dataBuilder.append("\n");
                dataBuilder.append(line.substring(5).stripLeading());
            } else if (line.startsWith("event:")) {
                eventName = line.substring(6).stripLeading();
            } else if (line.startsWith("id:")) {
                eventId = line.substring(3).stripLeading();
            }
        }

        if (!dataBuilder.isEmpty()) {
            emitter.send(SseEmitter.event().data(dataBuilder.toString()));
        }
    }

    private HttpURLConnection openSseConnection(String path) throws Exception {
        URI uri = URI.create(config.getBaseUrl() + path);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestProperty("Accept", "text/event-stream");
        if (config.hasAuth()) {
            String credentials = Base64.getEncoder().encodeToString(
                    (config.getUsername() + ":" + config.getPassword())
                            .getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + credentials);
        }
        conn.setConnectTimeout((int) Math.min(config.getTimeoutMs(), 5_000L));
        conn.setReadTimeout(0);
        return conn;
    }
}
```

- [ ] **Step 2: Register SseProxyHelper bean in OpencodeBeansConfig**

在 `OpencodeBeansConfig.java` 中，在 `// --- API Clients ---` 注释之前添加：

```java
    @Bean
    public SseProxyHelper sseProxyHelper(OpencodeClientConfig config, ExecutorService sseExecutor) {
        return new SseProxyHelper(config, sseExecutor, sseTimeoutMs);
    }
```

需要添加 import：`import com.example.opencodeapi.service.SseProxyHelper;`

- [ ] **Step 3: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/SseProxyHelper.java \
        src/main/java/com/example/opencodeapi/config/OpencodeBeansConfig.java
git commit -m "feat: add SseProxyHelper for SSE event stream proxying"
```

---

## Task 5: Simple GET Controllers (Path, Vcs, Command, Agent, Lsp, Formatter)

6 个 Service + 6 个 Controller + 6 个 Test，全部遵循相同模式：单个 GET 端点，返回 `ApiResponse<JsonNode>`。

**Files:**
- Create: `main/service/{Path,Vcs,Command,Agent,Lsp,Formatter}Service.java` — 6 files
- Create: `main/controller/{Path,Vcs,Command,Agent,Lsp,Formatter}Controller.java` — 6 files
- Create: `test/controller/{Path,Vcs,Command,Agent,Lsp,Formatter}ControllerTest.java` — 6 files

### Pattern（以 PathService/Controller/Test 为模板）

**Service 模板：**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.PathApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class PathService {

    private final PathApiClient pathApiClient;

    public JsonNode get() {
        return pathApiClient.get();
    }
}
```

**Controller 模板：**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.PathService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/path")
@RequiredArgsConstructor
public class PathController {

    private final PathService pathService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> get() {
        return ResponseEntity.ok(ApiResponse.ok(pathService.get()));
    }
}
```

**Test 模板：**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.service.PathService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PathController.class)
class PathControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PathService pathService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void get_returnsOk() throws Exception {
        when(pathService.get()).thenReturn(objectMapper.readTree("{\"path\":\"/home\"}"));

        mockMvc.perform(get("/api/v1/path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.path").value("/home"));
    }

    @Test
    void get_whenApiException_returnsErrorStatus() throws Exception {
        when(pathService.get()).thenThrow(new OpencodeApiException(404, "/path", "not found"));

        mockMvc.perform(get("/api/v1/path"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void get_whenConnectionException_returns502() throws Exception {
        when(pathService.get()).thenThrow(
                new OpencodeConnectionException("/path", new RuntimeException("timeout")));

        mockMvc.perform(get("/api/v1/path"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(502));
    }
}
```

### 各 Controller 差异对照表

| Service | API Client | Method | Controller Prefix | Endpoint |
|---------|-----------|--------|------------------|----------|
| `PathService` | `PathApiClient` | `get()` → `pathApiClient.get()` | `/api/v1/path` | `GET /` |
| `VcsService` | `VcsApiClient` | `get()` → `vcsApiClient.get()` | `/api/v1/vcs` | `GET /` |
| `CommandService` | `CommandApiClient` | `list()` → `commandApiClient.list()` | `/api/v1/command` | `GET /` |
| `AgentService` | `AgentApiClient` | `list()` → `agentApiClient.list()` | `/api/v1/agent` | `GET /` |
| `LspService` | `LspApiClient` | `getStatus()` → `lspApiClient.getStatus()` | `/api/v1/lsp` | `GET /` |
| `FormatterService` | `FormatterApiClient` | `getStatus()` → `formatterApiClient.getStatus()` | `/api/v1/formatter` | `GET /` |

- [ ] **Step 1: Create 6 Service files**

按上方 **Service 模板** 创建 6 个文件，替换类名、API Client 类型、方法名。参照差异对照表。

- [ ] **Step 2: Create 6 Controller files**

按上方 **Controller 模板** 创建 6 个文件，替换类名、Service 类型、RequestMapping 前缀、方法名。

- [ ] **Step 3: Create 6 Test files**

按上方 **Test 模板** 创建 6 个文件，替换类名、Service 类型、端点路径、mock 数据。每个 Test 包含 3 个测试方法：正常返回、API 异常、连接异常。

- [ ] **Step 4: Run all 6 tests**

Run: `./mvnw test -Dtest="PathControllerTest,VcsControllerTest,CommandControllerTest,AgentControllerTest,LspControllerTest,FormatterControllerTest" -pl . -q`
Expected: 18 tests PASS (3 per controller × 6)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/{Path,Vcs,Command,Agent,Lsp,Formatter}Service.java \
        src/main/java/com/example/opencodeapi/controller/{Path,Vcs,Command,Agent,Lsp,Formatter}Controller.java \
        src/test/java/com/example/opencodeapi/controller/{Path,Vcs,Command,Agent,Lsp,Formatter}ControllerTest.java
git commit -m "feat: add simple GET controllers (Path, Vcs, Command, Agent, Lsp, Formatter)"
```

---

## Task 6: ProjectController (2 GET endpoints)

**Files:**
- Create: `main/service/ProjectService.java`
- Create: `main/controller/ProjectController.java`
- Create: `test/controller/ProjectControllerTest.java`

- [ ] **Step 1: Create ProjectService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.ProjectApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectApiClient projectApiClient;

    public JsonNode list() {
        return projectApiClient.list();
    }

    public JsonNode getCurrent() {
        return projectApiClient.getCurrent();
    }
}
```

- [ ] **Step 2: Create ProjectController**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> list() {
        return ResponseEntity.ok(ApiResponse.ok(projectService.list()));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<JsonNode>> getCurrent() {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getCurrent()));
    }
}
```

- [ ] **Step 3: Write ProjectControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(projectService.list()).thenReturn(objectMapper.readTree("[{\"name\":\"proj1\"}]"));

        mockMvc.perform(get("/api/v1/project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("proj1"));
    }

    @Test
    void getCurrent_returnsOk() throws Exception {
        when(projectService.getCurrent()).thenReturn(objectMapper.readTree("{\"name\":\"current\"}"));

        mockMvc.perform(get("/api/v1/project/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("current"));
    }

    @Test
    void list_whenApiException_returnsErrorStatus() throws Exception {
        when(projectService.list()).thenThrow(new OpencodeApiException(500, "/project", "server error"));

        mockMvc.perform(get("/api/v1/project"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void list_whenConnectionException_returns502() throws Exception {
        when(projectService.list()).thenThrow(
                new OpencodeConnectionException("/project", new RuntimeException("unreachable")));

        mockMvc.perform(get("/api/v1/project"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(502));
    }
}
```

- [ ] **Step 4: Run test**

Run: `./mvnw test -Dtest=ProjectControllerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/ProjectService.java \
        src/main/java/com/example/opencodeapi/controller/ProjectController.java \
        src/test/java/com/example/opencodeapi/controller/ProjectControllerTest.java
git commit -m "feat: add ProjectController with list and getCurrent endpoints"
```

---

## Task 7: Instance + Log + Auth + Doc Controllers

4 个 Controller，各有特殊之处：Instance（POST）、Log（POST+body）、Auth（PUT+pathParam+body）、Doc（GET HTML, 不包裹 ApiResponse）。

**Files:**
- Create: `main/service/{Instance,Log,Auth,Doc}Service.java` — 4 files
- Create: `main/controller/{Instance,Log,Auth,Doc}Controller.java` — 4 files
- Create: `test/controller/{Instance,Log,Auth,Doc}ControllerTest.java` — 4 files

- [ ] **Step 1: Create InstanceService + Controller**

**InstanceService.java:**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.InstanceApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class InstanceService {

    private final InstanceApiClient instanceApiClient;

    public JsonNode dispose() {
        return instanceApiClient.dispose();
    }
}
```

**InstanceController.java:**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.InstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/instance")
@RequiredArgsConstructor
public class InstanceController {

    private final InstanceService instanceService;

    @PostMapping("/dispose")
    public ResponseEntity<ApiResponse<JsonNode>> dispose() {
        return ResponseEntity.ok(ApiResponse.ok(instanceService.dispose()));
    }
}
```

- [ ] **Step 2: Create LogService + Controller**

**LogService.java:**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.LogApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogApiClient logApiClient;

    public JsonNode write(String service, String level, String message, Map<String, Object> extra) {
        return logApiClient.write(service, level, message, extra);
    }
}
```

**LogController.java:**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping
    public ResponseEntity<ApiResponse<JsonNode>> write(@RequestBody Map<String, Object> body) {
        String service = (String) body.get("service");
        String level = (String) body.get("level");
        String message = (String) body.get("message");
        @SuppressWarnings("unchecked")
        Map<String, Object> extra = (Map<String, Object>) body.get("extra");
        return ResponseEntity.ok(ApiResponse.ok(logService.write(service, level, message, extra)));
    }
}
```

- [ ] **Step 3: Create AuthService + Controller**

**AuthService.java:**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.AuthApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthApiClient authApiClient;

    public JsonNode set(String id, Map<String, Object> body) {
        return authApiClient.set(id, body);
    }
}
```

**AuthController.java:**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JsonNode>> set(@PathVariable String id,
                                                     @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(authService.set(id, body)));
    }
}
```

- [ ] **Step 4: Create DocService + Controller (HTML response)**

**DocService.java:**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.DocApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocService {

    private final DocApiClient docApiClient;

    public String get() {
        return docApiClient.get();
    }
}
```

**DocController.java:**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.DocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doc")
@RequiredArgsConstructor
public class DocController {

    private final DocService docService;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String get() {
        return docService.get();
    }
}
```

- [ ] **Step 5: Write 4 test files**

**InstanceControllerTest.java:**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.InstanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstanceController.class)
class InstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstanceService instanceService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void dispose_returnsOk() throws Exception {
        when(instanceService.dispose()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/instance/dispose"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
```

**LogControllerTest.java:**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.LogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogService logService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void write_returnsOk() throws Exception {
        when(logService.write(anyString(), anyString(), anyString(), any()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"service\":\"test\",\"level\":\"info\",\"message\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

**AuthControllerTest.java:**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void set_returnsOk() throws Exception {
        when(authService.set(eq("provider-1"), any()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(put("/api/v1/auth/provider-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

**DocControllerTest.java:**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.DocService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocController.class)
class DocControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocService docService;

    @Test
    void get_returnsHtml() throws Exception {
        when(docService.get()).thenReturn("<html><body>Hello</body></html>");

        mockMvc.perform(get("/api/v1/doc").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string("<html><body>Hello</body></html>"));
    }
}
```

- [ ] **Step 6: Run all 4 tests**

Run: `./mvnw test -Dtest="InstanceControllerTest,LogControllerTest,AuthControllerTest,DocControllerTest" -pl . -q`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/{Instance,Log,Auth,Doc}Service.java \
        src/main/java/com/example/opencodeapi/controller/{Instance,Log,Auth,Doc}Controller.java \
        src/test/java/com/example/opencodeapi/controller/{Instance,Log,Auth,Doc}ControllerTest.java
git commit -m "feat: add Instance, Log, Auth, Doc controllers"
```

---

## Task 8: ConfigController (GET + PATCH + GET)

**Files:**
- Create: `main/service/ConfigService.java`
- Create: `main/controller/ConfigController.java`
- Create: `test/controller/ConfigControllerTest.java`

- [ ] **Step 1: Create ConfigService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.ConfigApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigApiClient configApiClient;

    public JsonNode get() {
        return configApiClient.get();
    }

    public JsonNode patch(Map<String, Object> updates) {
        return configApiClient.patch(updates);
    }

    public JsonNode getProviders() {
        return configApiClient.getProviders();
    }
}
```

- [ ] **Step 2: Create ConfigController**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> get() {
        return ResponseEntity.ok(ApiResponse.ok(configService.get()));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<JsonNode>> patch(@RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(ApiResponse.ok(configService.patch(updates)));
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<JsonNode>> getProviders() {
        return ResponseEntity.ok(ApiResponse.ok(configService.getProviders()));
    }
}
```

- [ ] **Step 3: Write ConfigControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.ConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigController.class)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfigService configService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void get_returnsOk() throws Exception {
        when(configService.get()).thenReturn(objectMapper.readTree("{\"theme\":\"dark\"}"));

        mockMvc.perform(get("/api/v1/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.theme").value("dark"));
    }

    @Test
    void patch_returnsOk() throws Exception {
        when(configService.patch(any())).thenReturn(objectMapper.readTree("{\"theme\":\"light\"}"));

        mockMvc.perform(patch("/api/v1/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"light\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.theme").value("light"));
    }

    @Test
    void getProviders_returnsOk() throws Exception {
        when(configService.getProviders()).thenReturn(objectMapper.readTree("[{\"id\":\"openai\"}]"));

        mockMvc.perform(get("/api/v1/config/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("openai"));
    }
}
```

- [ ] **Step 4: Run test**

Run: `./mvnw test -Dtest=ConfigControllerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/ConfigService.java \
        src/main/java/com/example/opencodeapi/controller/ConfigController.java \
        src/test/java/com/example/opencodeapi/controller/ConfigControllerTest.java
git commit -m "feat: add ConfigController with get, patch, getProviders"
```

---

## Task 9: ProviderController (4 endpoints with path params)

**Files:**
- Create: `main/service/ProviderService.java`
- Create: `main/controller/ProviderController.java`
- Create: `test/controller/ProviderControllerTest.java`

- [ ] **Step 1: Create ProviderService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.ProviderApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderApiClient providerApiClient;

    public JsonNode list() {
        return providerApiClient.list();
    }

    public JsonNode getAuth() {
        return providerApiClient.getAuth();
    }

    public JsonNode oauthAuthorize(String providerId) {
        return providerApiClient.oauthAuthorize(providerId);
    }

    public JsonNode oauthCallback(String providerId, Map<String, Object> body) {
        return providerApiClient.oauthCallback(providerId, body);
    }
}
```

- [ ] **Step 2: Create ProviderController**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/provider")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> list() {
        return ResponseEntity.ok(ApiResponse.ok(providerService.list()));
    }

    @GetMapping("/auth")
    public ResponseEntity<ApiResponse<JsonNode>> getAuth() {
        return ResponseEntity.ok(ApiResponse.ok(providerService.getAuth()));
    }

    @PostMapping("/{providerId}/oauth/authorize")
    public ResponseEntity<ApiResponse<JsonNode>> oauthAuthorize(@PathVariable String providerId) {
        return ResponseEntity.ok(ApiResponse.ok(providerService.oauthAuthorize(providerId)));
    }

    @PostMapping("/{providerId}/oauth/callback")
    public ResponseEntity<ApiResponse<JsonNode>> oauthCallback(@PathVariable String providerId,
                                                                @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(providerService.oauthCallback(providerId, body)));
    }
}
```

- [ ] **Step 3: Write ProviderControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProviderController.class)
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProviderService providerService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(providerService.list()).thenReturn(objectMapper.readTree("[{\"id\":\"openai\"}]"));

        mockMvc.perform(get("/api/v1/provider"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("openai"));
    }

    @Test
    void getAuth_returnsOk() throws Exception {
        when(providerService.getAuth()).thenReturn(objectMapper.readTree("{\"authenticated\":true}"));

        mockMvc.perform(get("/api/v1/provider/auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true));
    }

    @Test
    void oauthAuthorize_returnsOk() throws Exception {
        when(providerService.oauthAuthorize("openai"))
                .thenReturn(objectMapper.readTree("{\"url\":\"https://auth.example.com\"}"));

        mockMvc.perform(post("/api/v1/provider/openai/oauth/authorize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("https://auth.example.com"));
    }

    @Test
    void oauthCallback_returnsOk() throws Exception {
        when(providerService.oauthCallback(eq("openai"), any()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/provider/openai/oauth/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"abc123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
```

- [ ] **Step 4: Run test**

Run: `./mvnw test -Dtest=ProviderControllerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/ProviderService.java \
        src/main/java/com/example/opencodeapi/controller/ProviderController.java \
        src/test/java/com/example/opencodeapi/controller/ProviderControllerTest.java
git commit -m "feat: add ProviderController with list, auth, OAuth endpoints"
```

---

## Task 10: McpController (GET + POST)

**Files:**
- Create: `main/service/McpService.java`
- Create: `main/controller/McpController.java`
- Create: `test/controller/McpControllerTest.java`

- [ ] **Step 1: Create McpService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.McpApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class McpService {

    private final McpApiClient mcpApiClient;

    public JsonNode getStatus() {
        return mcpApiClient.getStatus();
    }

    public JsonNode add(String name, Map<String, Object> mcpConfig) {
        return mcpApiClient.add(name, mcpConfig);
    }
}
```

- [ ] **Step 2: Create McpController**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.McpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;

    @GetMapping
    public ResponseEntity<ApiResponse<JsonNode>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok(mcpService.getStatus()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JsonNode>> add(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> mcpConfig = (Map<String, Object>) body.get("config");
        return ResponseEntity.ok(ApiResponse.ok(mcpService.add(name, mcpConfig)));
    }
}
```

- [ ] **Step 3: Write McpControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.McpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(McpController.class)
class McpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private McpService mcpService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void getStatus_returnsOk() throws Exception {
        when(mcpService.getStatus()).thenReturn(objectMapper.readTree("{\"servers\":[]}"));

        mockMvc.perform(get("/api/v1/mcp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.servers").isArray());
    }

    @Test
    void add_returnsOk() throws Exception {
        when(mcpService.add(anyString(), any())).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test-mcp\",\"config\":{\"command\":\"npx\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
```

- [ ] **Step 4: Run test**

Run: `./mvnw test -Dtest=McpControllerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/McpService.java \
        src/main/java/com/example/opencodeapi/controller/McpController.java \
        src/test/java/com/example/opencodeapi/controller/McpControllerTest.java
git commit -m "feat: add McpController with getStatus and add endpoints"
```

---

## Task 11: SSE Controllers (GlobalController + EventController)

**Files:**
- Create: `main/service/GlobalService.java`
- Create: `main/service/EventService.java`
- Create: `main/controller/GlobalController.java`
- Create: `main/controller/EventController.java`
- Create: `test/controller/GlobalControllerTest.java`
- Create: `test/controller/EventControllerTest.java`

- [ ] **Step 1: Create GlobalService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.GlobalApiClient;
import com.example.opencodeapi.client.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class GlobalService {

    private final GlobalApiClient globalApiClient;
    private final SseProxyHelper sseProxyHelper;

    public HealthResponse getHealth() {
        return globalApiClient.getHealth();
    }

    public SseEmitter getGlobalEvent(Long timeout) {
        long resolvedTimeout = sseProxyHelper.resolveTimeout(timeout);
        return sseProxyHelper.proxy("/global/event", resolvedTimeout);
    }
}
```

- [ ] **Step 2: Create EventService**

```java
package com.example.opencodeapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class EventService {

    private final SseProxyHelper sseProxyHelper;

    public SseEmitter getEvent(Long timeout) {
        long resolvedTimeout = sseProxyHelper.resolveTimeout(timeout);
        return sseProxyHelper.proxy("/event", resolvedTimeout);
    }
}
```

- [ ] **Step 3: Create GlobalController**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.dto.HealthResponse;
import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.GlobalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/global")
@RequiredArgsConstructor
public class GlobalController {

    private final GlobalService globalService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponse>> health() {
        return ResponseEntity.ok(ApiResponse.ok(globalService.getHealth()));
    }

    @GetMapping(value = "/event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter event(@RequestParam(required = false) Long timeout) {
        return globalService.getGlobalEvent(timeout);
    }
}
```

- [ ] **Step 4: Create EventController**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter event(@RequestParam(required = false) Long timeout) {
        return eventService.getEvent(timeout);
    }
}
```

- [ ] **Step 5: Write GlobalControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.dto.HealthResponse;
import com.example.opencodeapi.service.GlobalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalController.class)
class GlobalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalService globalService;

    @Test
    void health_returnsOk() throws Exception {
        HealthResponse health = new HealthResponse();
        health.setHealthy(true);
        health.setVersion("1.0.0");
        when(globalService.getHealth()).thenReturn(health);

        mockMvc.perform(get("/api/v1/global/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.healthy").value(true))
                .andExpect(jsonPath("$.data.version").value("1.0.0"));
    }

    @Test
    void event_returnsSseEmitter() throws Exception {
        when(globalService.getGlobalEvent(any())).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/v1/global/event"))
                .andExpect(request().asyncStarted());
    }
}
```

- [ ] **Step 6: Write EventControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void event_returnsSseEmitter() throws Exception {
        when(eventService.getEvent(any())).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/v1/event"))
                .andExpect(request().asyncStarted());
    }
}
```

- [ ] **Step 7: Run tests**

Run: `./mvnw test -Dtest="GlobalControllerTest,EventControllerTest" -pl . -q`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/{Global,Event}Service.java \
        src/main/java/com/example/opencodeapi/controller/{Global,Event}Controller.java \
        src/test/java/com/example/opencodeapi/controller/{Global,Event}ControllerTest.java
git commit -m "feat: add GlobalController and EventController with SSE support"
```

---

## Task 12: SessionController (18 endpoints)

**Files:**
- Create: `main/service/SessionService.java`
- Create: `main/controller/SessionController.java`
- Create: `test/controller/SessionControllerTest.java`

- [ ] **Step 1: Create SessionService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.SessionApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionApiClient sessionApiClient;

    public JsonNode list() {
        return sessionApiClient.list();
    }

    public JsonNode create(String parentId, String title) {
        return sessionApiClient.create(parentId, title);
    }

    public JsonNode getStatus() {
        return sessionApiClient.getStatus();
    }

    public JsonNode get(String id) {
        return sessionApiClient.get(id);
    }

    public JsonNode delete(String id) {
        return sessionApiClient.delete(id);
    }

    public JsonNode patch(String id, Map<String, Object> body) {
        return sessionApiClient.patch(id, body);
    }

    public JsonNode getChildren(String id) {
        return sessionApiClient.getChildren(id);
    }

    public JsonNode getTodo(String id) {
        return sessionApiClient.getTodo(id);
    }

    public JsonNode init(String id, Map<String, Object> body) {
        return sessionApiClient.init(id, body);
    }

    public JsonNode fork(String id, String messageId) {
        return sessionApiClient.fork(id, messageId);
    }

    public JsonNode abort(String id) {
        return sessionApiClient.abort(id);
    }

    public JsonNode share(String id) {
        return sessionApiClient.share(id);
    }

    public JsonNode unshare(String id) {
        return sessionApiClient.unshare(id);
    }

    public JsonNode getDiff(String id, String messageId) {
        return sessionApiClient.getDiff(id, messageId);
    }

    public JsonNode summarize(String id, Map<String, Object> body) {
        return sessionApiClient.summarize(id, body);
    }

    public JsonNode revert(String id, Map<String, Object> body) {
        return sessionApiClient.revert(id, body);
    }

    public JsonNode unrevert(String id) {
        return sessionApiClient.unrevert(id);
    }

    public JsonNode respondToPermission(String id, String permissionId, Map<String, Object> body) {
        return sessionApiClient.respondToPermission(id, permissionId, body);
    }
}
```

- [ ] **Step 2: Create SessionController**

```java
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
```

- [ ] **Step 3: Write SessionControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(sessionService.list()).thenReturn(objectMapper.readTree("[{\"id\":\"s1\"}]"));

        mockMvc.perform(get("/api/v1/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("s1"));
    }

    @Test
    void create_returnsOk() throws Exception {
        when(sessionService.create(any(), any())).thenReturn(objectMapper.readTree("{\"id\":\"new\"}"));

        mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("new"));
    }

    @Test
    void getStatus_returnsOk() throws Exception {
        when(sessionService.getStatus()).thenReturn(objectMapper.readTree("{\"active\":true}"));

        mockMvc.perform(get("/api/v1/session/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void getById_returnsOk() throws Exception {
        when(sessionService.get("s1")).thenReturn(objectMapper.readTree("{\"id\":\"s1\"}"));

        mockMvc.perform(get("/api/v1/session/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("s1"));
    }

    @Test
    void patchById_returnsOk() throws Exception {
        when(sessionService.patch(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"id\":\"s1\"}"));

        mockMvc.perform(patch("/api/v1/session/s1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteById_returnsOk() throws Exception {
        when(sessionService.delete("s1")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(delete("/api/v1/session/s1"))
                .andExpect(status().isOk());
    }

    @Test
    void getChildren_returnsOk() throws Exception {
        when(sessionService.getChildren("s1")).thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/session/s1/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getTodo_returnsOk() throws Exception {
        when(sessionService.getTodo("s1")).thenReturn(objectMapper.readTree("{\"items\":[]}"));

        mockMvc.perform(get("/api/v1/session/s1/todo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void init_returnsOk() throws Exception {
        when(sessionService.init(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void fork_returnsOk() throws Exception {
        when(sessionService.fork(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"id\":\"forked\"}"));

        mockMvc.perform(post("/api/v1/session/s1/fork")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messageID\":\"m1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("forked"));
    }

    @Test
    void abort_returnsOk() throws Exception {
        when(sessionService.abort("s1")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/abort"))
                .andExpect(status().isOk());
    }

    @Test
    void share_returnsOk() throws Exception {
        when(sessionService.share("s1")).thenReturn(objectMapper.readTree("{\"url\":\"http://...\"}"));

        mockMvc.perform(post("/api/v1/session/s1/share"))
                .andExpect(status().isOk());
    }

    @Test
    void unshare_returnsOk() throws Exception {
        when(sessionService.unshare("s1")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(delete("/api/v1/session/s1/share"))
                .andExpect(status().isOk());
    }

    @Test
    void getDiff_withMessageId_returnsOk() throws Exception {
        when(sessionService.getDiff("s1", "m1")).thenReturn(objectMapper.readTree("{\"diff\":\"...\"}"));

        mockMvc.perform(get("/api/v1/session/s1/diff").param("messageID", "m1"))
                .andExpect(status().isOk());
    }

    @Test
    void summarize_returnsOk() throws Exception {
        when(sessionService.summarize(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"summary\":\"...\"}"));

        mockMvc.perform(post("/api/v1/session/s1/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provider\":\"openai\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void revert_returnsOk() throws Exception {
        when(sessionService.revert(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/revert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messageID\":\"m1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void unrevert_returnsOk() throws Exception {
        when(sessionService.unrevert("s1")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/unrevert"))
                .andExpect(status().isOk());
    }

    @Test
    void respondToPermission_returnsOk() throws Exception {
        when(sessionService.respondToPermission(eq("s1"), eq("p1"), any()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/session/s1/permissions/p1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"allow\":true}"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 4: Run test**

Run: `./mvnw test -Dtest=SessionControllerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/SessionService.java \
        src/main/java/com/example/opencodeapi/controller/SessionController.java \
        src/test/java/com/example/opencodeapi/controller/SessionControllerTest.java
git commit -m "feat: add SessionController with 18 session management endpoints"
```

---

## Task 13: MessageController (dual prefix, 6 endpoints)

**Files:**
- Create: `main/service/MessageService.java`
- Create: `main/controller/MessageController.java`
- Create: `test/controller/MessageControllerTest.java`

- [ ] **Step 1: Create MessageService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.MessageApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageApiClient messageApiClient;

    public JsonNode list(String sessionId, Integer limit) {
        return messageApiClient.list(sessionId, limit);
    }

    public JsonNode send(String sessionId, Map<String, Object> body) {
        return messageApiClient.send(sessionId, body);
    }

    public JsonNode get(String sessionId, String messageId) {
        return messageApiClient.get(sessionId, messageId);
    }

    public void promptAsync(String sessionId, Map<String, Object> body) {
        messageApiClient.promptAsync(sessionId, body);
    }

    public JsonNode executeCommand(String sessionId, Map<String, Object> body) {
        return messageApiClient.executeCommand(sessionId, body);
    }

    public JsonNode executeShell(String sessionId, Map<String, Object> body) {
        return messageApiClient.executeShell(sessionId, body);
    }
}
```

- [ ] **Step 2: Create MessageController**

使用类级别 `@RequestMapping("/api/v1/session/{sessionId}")` 统一路径前缀：

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/session/{sessionId}")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/message")
    public ResponseEntity<ApiResponse<JsonNode>> list(@PathVariable String sessionId,
                                                      @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.list(sessionId, limit)));
    }

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<JsonNode>> send(@PathVariable String sessionId,
                                                      @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.send(sessionId, body)));
    }

    @GetMapping("/message/{messageId}")
    public ResponseEntity<ApiResponse<JsonNode>> get(@PathVariable String sessionId,
                                                     @PathVariable String messageId) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.get(sessionId, messageId)));
    }

    @PostMapping("/prompt_async")
    public ResponseEntity<Void> promptAsync(@PathVariable String sessionId,
                                            @RequestBody Map<String, Object> body) {
        messageService.promptAsync(sessionId, body);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/command")
    public ResponseEntity<ApiResponse<JsonNode>> executeCommand(@PathVariable String sessionId,
                                                                @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.executeCommand(sessionId, body)));
    }

    @PostMapping("/shell")
    public ResponseEntity<ApiResponse<JsonNode>> executeShell(@PathVariable String sessionId,
                                                              @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(messageService.executeShell(sessionId, body)));
    }
}
```

- [ ] **Step 3: Write MessageControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void list_returnsOk() throws Exception {
        when(messageService.list("s1", null)).thenReturn(objectMapper.readTree("[{\"id\":\"m1\"}]"));

        mockMvc.perform(get("/api/v1/session/s1/message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("m1"));
    }

    @Test
    void list_withLimit_returnsOk() throws Exception {
        when(messageService.list("s1", 10)).thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/session/s1/message").param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void send_returnsOk() throws Exception {
        when(messageService.send(eq("s1"), any())).thenReturn(objectMapper.readTree("{\"id\":\"m2\"}"));

        mockMvc.perform(post("/api/v1/session/s1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("m2"));
    }

    @Test
    void getById_returnsOk() throws Exception {
        when(messageService.get("s1", "m1")).thenReturn(objectMapper.readTree("{\"id\":\"m1\"}"));

        mockMvc.perform(get("/api/v1/session/s1/message/m1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("m1"));
    }

    @Test
    void promptAsync_returns204() throws Exception {
        doNothing().when(messageService).promptAsync(eq("s1"), any());

        mockMvc.perform(post("/api/v1/session/s1/prompt_async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void executeCommand_returnsOk() throws Exception {
        when(messageService.executeCommand(eq("s1"), any()))
                .thenReturn(objectMapper.readTree("{\"result\":\"ok\"}"));

        mockMvc.perform(post("/api/v1/session/s1/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void executeShell_returnsOk() throws Exception {
        when(messageService.executeShell(eq("s1"), any()))
                .thenReturn(objectMapper.readTree("{\"output\":\"done\"}"));

        mockMvc.perform(post("/api/v1/session/s1/shell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"ls\"}"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 4: Run test**

Run: `./mvnw test -Dtest=MessageControllerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/MessageService.java \
        src/main/java/com/example/opencodeapi/controller/MessageController.java \
        src/test/java/com/example/opencodeapi/controller/MessageControllerTest.java
git commit -m "feat: add MessageController with message, prompt_async, command, shell"
```

---

## Task 14: FileController (no class-level mapping, 6 endpoints)

**Files:**
- Create: `main/service/FileService.java`
- Create: `main/controller/FileController.java`
- Create: `test/controller/FileControllerTest.java`

- [ ] **Step 1: Create FileService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.FileApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileApiClient fileApiClient;

    public JsonNode find(String pattern) {
        return fileApiClient.find(pattern);
    }

    public JsonNode findFile(String query, String type, String directory, Integer limit, String dirs) {
        return fileApiClient.findFile(query, type, directory, limit, dirs);
    }

    public JsonNode findSymbol(String query) {
        return fileApiClient.findSymbol(query);
    }

    public JsonNode list(String path) {
        return fileApiClient.list(path);
    }

    public JsonNode getContent(String path) {
        return fileApiClient.getContent(path);
    }

    public JsonNode getStatus() {
        return fileApiClient.getStatus();
    }
}
```

- [ ] **Step 2: Create FileController (no class-level @RequestMapping)**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/api/v1/find")
    public ResponseEntity<ApiResponse<JsonNode>> find(@RequestParam String pattern) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.find(pattern)));
    }

    @GetMapping("/api/v1/find/file")
    public ResponseEntity<ApiResponse<JsonNode>> findFile(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String dirs) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.findFile(query, type, directory, limit, dirs)));
    }

    @GetMapping("/api/v1/find/symbol")
    public ResponseEntity<ApiResponse<JsonNode>> findSymbol(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.findSymbol(query)));
    }

    @GetMapping("/api/v1/file")
    public ResponseEntity<ApiResponse<JsonNode>> list(@RequestParam String path) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.list(path)));
    }

    @GetMapping("/api/v1/file/content")
    public ResponseEntity<ApiResponse<JsonNode>> getContent(@RequestParam String path) {
        return ResponseEntity.ok(ApiResponse.ok(fileService.getContent(path)));
    }

    @GetMapping("/api/v1/file/status")
    public ResponseEntity<ApiResponse<JsonNode>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok(fileService.getStatus()));
    }
}
```

- [ ] **Step 3: Write FileControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void find_returnsOk() throws Exception {
        when(fileService.find("*.java")).thenReturn(objectMapper.readTree("[\"App.java\"]"));

        mockMvc.perform(get("/api/v1/find").param("pattern", "*.java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void findFile_returnsOk() throws Exception {
        when(fileService.findFile(eq("test"), any(), any(), any(), any()))
                .thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/find/file").param("query", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void findSymbol_returnsOk() throws Exception {
        when(fileService.findSymbol("MyClass")).thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/find/symbol").param("query", "MyClass"))
                .andExpect(status().isOk());
    }

    @Test
    void list_returnsOk() throws Exception {
        when(fileService.list("/src")).thenReturn(objectMapper.readTree("[\"file1.java\"]"));

        mockMvc.perform(get("/api/v1/file").param("path", "/src"))
                .andExpect(status().isOk());
    }

    @Test
    void getContent_returnsOk() throws Exception {
        when(fileService.getContent("/src/App.java"))
                .thenReturn(objectMapper.readTree("{\"content\":\"...\"}"));

        mockMvc.perform(get("/api/v1/file/content").param("path", "/src/App.java"))
                .andExpect(status().isOk());
    }

    @Test
    void getStatus_returnsOk() throws Exception {
        when(fileService.getStatus()).thenReturn(objectMapper.readTree("{\"tracked\":10}"));

        mockMvc.perform(get("/api/v1/file/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tracked").value(10));
    }
}
```

- [ ] **Step 4: Run test**

Run: `./mvnw test -Dtest=FileControllerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/FileService.java \
        src/main/java/com/example/opencodeapi/controller/FileController.java \
        src/test/java/com/example/opencodeapi/controller/FileControllerTest.java
git commit -m "feat: add FileController with find and file endpoints"
```

---

## Task 15: TuiController (11 endpoints, special timeout on controlNext)

**Files:**
- Create: `main/service/TuiService.java`
- Create: `main/controller/TuiController.java`
- Create: `test/controller/TuiControllerTest.java`

- [ ] **Step 1: Create TuiService**

```java
package com.example.opencodeapi.service;

import com.example.opencodeapi.client.api.TuiApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TuiService {

    private final TuiApiClient tuiApiClient;

    public JsonNode appendPrompt(String text) {
        return tuiApiClient.appendPrompt(text);
    }

    public JsonNode openHelp() {
        return tuiApiClient.openHelp();
    }

    public JsonNode openSessions() {
        return tuiApiClient.openSessions();
    }

    public JsonNode openThemes() {
        return tuiApiClient.openThemes();
    }

    public JsonNode openModels() {
        return tuiApiClient.openModels();
    }

    public JsonNode submitPrompt() {
        return tuiApiClient.submitPrompt();
    }

    public JsonNode clearPrompt() {
        return tuiApiClient.clearPrompt();
    }

    public JsonNode executeCommand(String command) {
        return tuiApiClient.executeCommand(command);
    }

    public JsonNode showToast(String title, String message, String variant) {
        return tuiApiClient.showToast(title, message, variant);
    }

    public JsonNode controlNext() {
        return tuiApiClient.controlNext();
    }

    public JsonNode controlResponse(Map<String, Object> body) {
        return tuiApiClient.controlResponse(body);
    }
}
```

- [ ] **Step 2: Create TuiController (with controlNext timeout handling)**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.dto.ApiResponse;
import com.example.opencodeapi.service.TuiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tui")
@RequiredArgsConstructor
public class TuiController {

    private final TuiService tuiService;

    @PostMapping("/append-prompt")
    public ResponseEntity<ApiResponse<JsonNode>> appendPrompt(@RequestBody Map<String, Object> body) {
        String text = (String) body.get("text");
        return ResponseEntity.ok(ApiResponse.ok(tuiService.appendPrompt(text)));
    }

    @PostMapping("/open-help")
    public ResponseEntity<ApiResponse<JsonNode>> openHelp() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.openHelp()));
    }

    @PostMapping("/open-sessions")
    public ResponseEntity<ApiResponse<JsonNode>> openSessions() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.openSessions()));
    }

    @PostMapping("/open-themes")
    public ResponseEntity<ApiResponse<JsonNode>> openThemes() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.openThemes()));
    }

    @PostMapping("/open-models")
    public ResponseEntity<ApiResponse<JsonNode>> openModels() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.openModels()));
    }

    @PostMapping("/submit-prompt")
    public ResponseEntity<ApiResponse<JsonNode>> submitPrompt() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.submitPrompt()));
    }

    @PostMapping("/clear-prompt")
    public ResponseEntity<ApiResponse<JsonNode>> clearPrompt() {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.clearPrompt()));
    }

    @PostMapping("/execute-command")
    public ResponseEntity<ApiResponse<JsonNode>> executeCommand(@RequestBody Map<String, Object> body) {
        String command = (String) body.get("command");
        return ResponseEntity.ok(ApiResponse.ok(tuiService.executeCommand(command)));
    }

    @PostMapping("/show-toast")
    public ResponseEntity<ApiResponse<JsonNode>> showToast(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String message = (String) body.get("message");
        String variant = (String) body.get("variant");
        return ResponseEntity.ok(ApiResponse.ok(tuiService.showToast(title, message, variant)));
    }

    @GetMapping("/control/next")
    public ResponseEntity<ApiResponse<JsonNode>> controlNext() {
        try {
            JsonNode result = tuiService.controlNext();
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (OpencodeConnectionException e) {
            log.debug("controlNext timed out, returning empty response");
            return ResponseEntity.ok(ApiResponse.ok(null));
        }
    }

    @PostMapping("/control/response")
    public ResponseEntity<ApiResponse<JsonNode>> controlResponse(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(tuiService.controlResponse(body)));
    }
}
```

- [ ] **Step 3: Write TuiControllerTest**

```java
package com.example.opencodeapi.controller;

import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.example.opencodeapi.service.TuiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TuiController.class)
class TuiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TuiService tuiService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void appendPrompt_returnsOk() throws Exception {
        when(tuiService.appendPrompt("hello")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/append-prompt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }

    @Test
    void openHelp_returnsOk() throws Exception {
        when(tuiService.openHelp()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/open-help"))
                .andExpect(status().isOk());
    }

    @Test
    void openSessions_returnsOk() throws Exception {
        when(tuiService.openSessions()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/open-sessions"))
                .andExpect(status().isOk());
    }

    @Test
    void openThemes_returnsOk() throws Exception {
        when(tuiService.openThemes()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/open-themes"))
                .andExpect(status().isOk());
    }

    @Test
    void openModels_returnsOk() throws Exception {
        when(tuiService.openModels()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/open-models"))
                .andExpect(status().isOk());
    }

    @Test
    void submitPrompt_returnsOk() throws Exception {
        when(tuiService.submitPrompt()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/submit-prompt"))
                .andExpect(status().isOk());
    }

    @Test
    void clearPrompt_returnsOk() throws Exception {
        when(tuiService.clearPrompt()).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/clear-prompt"))
                .andExpect(status().isOk());
    }

    @Test
    void executeCommand_returnsOk() throws Exception {
        when(tuiService.executeCommand("test")).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/execute-command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"command\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void showToast_returnsOk() throws Exception {
        when(tuiService.showToast(any(), anyString(), anyString()))
                .thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/show-toast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Info\",\"message\":\"Hello\",\"variant\":\"info\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void controlNext_returnsOk() throws Exception {
        when(tuiService.controlNext()).thenReturn(objectMapper.readTree("{\"type\":\"input\"}"));

        mockMvc.perform(get("/api/v1/tui/control/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("input"));
    }

    @Test
    void controlNext_whenTimeout_returnsEmptyOk() throws Exception {
        when(tuiService.controlNext()).thenThrow(
                new OpencodeConnectionException("/tui/control/next", new RuntimeException("timeout")));

        mockMvc.perform(get("/api/v1/tui/control/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void controlResponse_returnsOk() throws Exception {
        when(tuiService.controlResponse(any())).thenReturn(objectMapper.readTree("{\"ok\":true}"));

        mockMvc.perform(post("/api/v1/tui/control/response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"yes\"}"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 4: Run test**

Run: `./mvnw test -Dtest=TuiControllerTest -pl . -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/example/opencodeapi/service/TuiService.java \
        src/main/java/com/example/opencodeapi/controller/TuiController.java \
        src/test/java/com/example/opencodeapi/controller/TuiControllerTest.java
git commit -m "feat: add TuiController with 11 endpoints and controlNext timeout handling"
```

---

## Task 16: Full Build Verification

- [ ] **Step 1: Run all unit tests**

Run: `./mvnw test -pl . -q`
Expected: All tests PASS

- [ ] **Step 2: Run full build (skip integration tests)**

Run: `SKIP_OPENCODE_IT=true ./mvnw clean package -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Verify endpoint count**

Run: `grep -r "@GetMapping\|@PostMapping\|@PatchMapping\|@PutMapping\|@DeleteMapping" src/main/java/com/example/opencodeapi/controller/ | wc -l`
Expected: 65（spec 标注 64 有一处漏计，实际从端点表逐行统计为 65）

- [ ] **Step 4: Verify file count**

Run: `find src/main/java/com/example/opencodeapi/{controller,service,dto,exception,config} -name "*.java" | wc -l`
Expected: 44 (20 controllers + 20 services + 1 SseProxyHelper + 1 ApiResponse + 1 GlobalExceptionHandler + 1 OpencodeBeansConfig = 44，不含 client 包)

- [ ] **Step 5: Final commit if any fixes**

```bash
git add -A
git status
# Only commit if there are changes
git commit -m "chore: fix any issues found during build verification"
```
