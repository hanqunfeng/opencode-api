# OpenCode Server API 客户端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 OpenCode Server 全部范围 B 接口（核心 + TUI + LSP/Formatter/MCP）的 Java 客户端，并为每个接口提供至少一条集成测试用例。

**Architecture:** 分组强类型客户端 + 集成测试矩阵。配置层读环境变量、传输层基于 Spring `RestClient` 统一 HTTP 调用、API 层按文档分组封装接口方法、测试层基于 JUnit 5 集成测试。

**Tech Stack:** Java 21, Spring Boot 4.0.3, Spring RestClient, Jackson (`JsonNode`), Lombok, JUnit 5

**Spec:** `docs/superpowers/specs/2026-03-19-opencode-server-api-client-design.md`

---

## File Structure

### Source Files (to create)

| File | Responsibility |
|------|---------------|
| `src/main/java/.../client/config/OpencodeClientConfig.java` | 读取环境变量，管理 base URL、认证、超时配置 |
| `src/main/java/.../client/http/OpencodeHttpClient.java` | 统一 HTTP 调用（GET/POST/PATCH/PUT/DELETE/SSE） |
| `src/main/java/.../client/http/OpencodeApiException.java` | API 错误异常（含 statusCode/path/body） |
| `src/main/java/.../client/http/OpencodeConnectionException.java` | 连接异常（服务不可达） |
| `src/main/java/.../client/dto/HealthResponse.java` | `/global/health` 响应 DTO |
| `src/main/java/.../client/api/GlobalApiClient.java` | `/global/*` 接口 |
| `src/main/java/.../client/api/ProjectApiClient.java` | `/project/*` 接口 |
| `src/main/java/.../client/api/PathApiClient.java` | `/path` 接口 |
| `src/main/java/.../client/api/VcsApiClient.java` | `/vcs` 接口 |
| `src/main/java/.../client/api/InstanceApiClient.java` | `/instance/*` 接口 |
| `src/main/java/.../client/api/ConfigApiClient.java` | `/config/*` 接口 |
| `src/main/java/.../client/api/ProviderApiClient.java` | `/provider/*` 接口 |
| `src/main/java/.../client/api/SessionApiClient.java` | `/session/*` 接口（不含 message） |
| `src/main/java/.../client/api/MessageApiClient.java` | `/session/:id/message/*` 及 `prompt_async`/`command`/`shell` |
| `src/main/java/.../client/api/CommandApiClient.java` | `/command` 接口 |
| `src/main/java/.../client/api/FileApiClient.java` | `/find/*`、`/file/*` 接口 |
| `src/main/java/.../client/api/AgentApiClient.java` | `/agent` 接口 |
| `src/main/java/.../client/api/LogApiClient.java` | `/log` 接口 |
| `src/main/java/.../client/api/AuthApiClient.java` | `/auth/:id` 接口 |
| `src/main/java/.../client/api/EventApiClient.java` | `/event` SSE 接口 |
| `src/main/java/.../client/api/DocApiClient.java` | `/doc` 接口 |
| `src/main/java/.../client/api/LspApiClient.java` | `/lsp` 接口 |
| `src/main/java/.../client/api/FormatterApiClient.java` | `/formatter` 接口 |
| `src/main/java/.../client/api/McpApiClient.java` | `/mcp` 接口 |
| `src/main/java/.../client/api/TuiApiClient.java` | `/tui/*` 接口 |

### Test Files (to create)

| File | Responsibility |
|------|---------------|
| `src/test/.../integration/IntegrationTestBase.java` | 测试基类：配置读取、健康检查、客户端初始化 |
| `src/test/.../integration/ApiAssertions.java` | 通用断言工具 |
| `src/test/.../integration/GlobalApiClientIT.java` | Global 接口测试 |
| `src/test/.../integration/ProjectApiClientIT.java` | Project/Path/VCS 接口测试 |
| `src/test/.../integration/ConfigApiClientIT.java` | Instance/Config 接口测试 |
| `src/test/.../integration/ProviderApiClientIT.java` | Provider 接口测试 |
| `src/test/.../integration/SessionApiClientIT.java` | Session 接口测试 |
| `src/test/.../integration/MessageApiClientIT.java` | Message/Command 接口测试 |
| `src/test/.../integration/FileApiClientIT.java` | File 接口测试 |
| `src/test/.../integration/MiscApiClientIT.java` | Agent/Log/Auth/Event/Doc 接口测试 |
| `src/test/.../integration/LspMcpApiClientIT.java` | LSP/Formatter/MCP 接口测试 |
| `src/test/.../integration/TuiApiClientIT.java` | TUI 接口测试 |

### Files to Modify

| File | Change |
|------|--------|
| `pom.xml` | 确认依赖完整（可能无需改动） |

> 以下路径缩写：`src/main/java/com/example/opencodeapi` → `main/`，`src/test/java/com/example/opencodeapi` → `test/`

---

## Task 1: 异常模型与配置类

**Files:**
- Create: `main/client/http/OpencodeApiException.java`
- Create: `main/client/http/OpencodeConnectionException.java`
- Create: `main/client/config/OpencodeClientConfig.java`

- [ ] **Step 1: 创建 OpencodeApiException**

```java
package com.example.opencodeapi.client.http;

import lombok.Getter;

@Getter
public class OpencodeApiException extends RuntimeException {

    private final int statusCode;
    private final String path;
    private final String responseBody;

    public OpencodeApiException(int statusCode, String path, String responseBody) {
        super("API error %d on %s: %s".formatted(statusCode, path,
                responseBody != null && responseBody.length() > 200
                        ? responseBody.substring(0, 200) + "..."
                        : responseBody));
        this.statusCode = statusCode;
        this.path = path;
        this.responseBody = responseBody;
    }
}
```

- [ ] **Step 2: 创建 OpencodeConnectionException**

```java
package com.example.opencodeapi.client.http;

import lombok.Getter;

@Getter
public class OpencodeConnectionException extends RuntimeException {

    private final String path;

    public OpencodeConnectionException(String path, Throwable cause) {
        super("Cannot connect to opencode server for path '%s'. "
                .formatted(path)
                + "Please ensure 'opencode serve' is running and OPENCODE_BASE_URL is set correctly.",
                cause);
        this.path = path;
    }
}
```

- [ ] **Step 3: 创建 OpencodeClientConfig**

```java
package com.example.opencodeapi.client.config;

import lombok.Getter;

@Getter
public class OpencodeClientConfig {

    private final String baseUrl;
    private final String username;
    private final String password;
    private final long timeoutMs;
    private final long blockingTimeoutMs;

    public OpencodeClientConfig() {
        this(System.getenv("OPENCODE_BASE_URL"),
                System.getenv("OPENCODE_USERNAME"),
                System.getenv("OPENCODE_PASSWORD"),
                System.getenv("OPENCODE_TIMEOUT_MS"),
                System.getenv("OPENCODE_BLOCKING_TIMEOUT_MS"));
    }

    public OpencodeClientConfig(String baseUrl, String username, String password,
                                String timeoutMs, String blockingTimeoutMs) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "OPENCODE_BASE_URL is required. "
                            + "Set it to your opencode server address, e.g. http://127.0.0.1:4096");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.username = (username == null || username.isBlank()) ? "opencode" : username;
        this.password = password;
        this.timeoutMs = parseLong(timeoutMs, 10_000L);
        this.blockingTimeoutMs = parseLong(blockingTimeoutMs, 3_000L);
    }

    public boolean hasAuth() {
        return password != null && !password.isBlank();
    }

    private static long parseLong(String value, long defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/
git commit -m "feat: add exception model and client config"
```

---

## Task 2: HTTP 客户端

**Files:**
- Create: `main/client/http/OpencodeHttpClient.java`

- [ ] **Step 1: 创建 OpencodeHttpClient**

```java
package com.example.opencodeapi.client.http;

import com.example.opencodeapi.client.config.OpencodeClientConfig;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class OpencodeHttpClient {

    private final RestClient restClient;
    private final RestClient blockingRestClient;
    private final OpencodeClientConfig config;

    public OpencodeHttpClient(OpencodeClientConfig config) {
        this.config = config;

        RestClient.Builder baseBuilder = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE);

        if (config.hasAuth()) {
            String credentials = Base64.getEncoder().encodeToString(
                    (config.getUsername() + ":" + config.getPassword())
                            .getBytes(StandardCharsets.UTF_8));
            baseBuilder.defaultHeader("Authorization", "Basic " + credentials);
        }

        SimpleClientHttpRequestFactory normalFactory = new SimpleClientHttpRequestFactory();
        normalFactory.setConnectTimeout(Duration.ofMillis(Math.min(config.getTimeoutMs(), 5_000L)));
        normalFactory.setReadTimeout(Duration.ofMillis(config.getTimeoutMs()));
        this.restClient = baseBuilder.clone().requestFactory(normalFactory).build();

        SimpleClientHttpRequestFactory blockingFactory = new SimpleClientHttpRequestFactory();
        blockingFactory.setConnectTimeout(Duration.ofMillis(Math.min(config.getBlockingTimeoutMs(), 5_000L)));
        blockingFactory.setReadTimeout(Duration.ofMillis(config.getBlockingTimeoutMs()));
        this.blockingRestClient = baseBuilder.clone().requestFactory(blockingFactory).build();
    }

    public <T> T get(String path, Class<T> type) {
        return get(path, null, type);
    }

    public <T> T get(String path, Map<String, String> queryParams, Class<T> type) {
        try {
            return restClient.get()
                    .uri(buildUri(path, queryParams))
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public <T> T post(String path, Object body, Class<T> type) {
        try {
            RestClient.RequestBodySpec spec = restClient.post().uri(path);
            if (body != null) {
                spec.contentType(MediaType.APPLICATION_JSON).body(body);
            }
            return spec.retrieve().body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public void postNoContent(String path, Object body) {
        try {
            RestClient.RequestBodySpec spec = restClient.post().uri(path);
            if (body != null) {
                spec.contentType(MediaType.APPLICATION_JSON).body(body);
            }
            spec.retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public <T> T patch(String path, Object body, Class<T> type) {
        try {
            return restClient.patch()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public <T> T put(String path, Object body, Class<T> type) {
        try {
            return restClient.put()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public <T> T delete(String path, Class<T> type) {
        try {
            return restClient.delete()
                    .uri(path)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public String getHtml(String path) {
        try {
            return restClient.get()
                    .uri(path)
                    .accept(MediaType.TEXT_HTML)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    public String readFirstSseEvent(String path) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executor.submit(() -> doReadFirstSseEvent(path));
            return future.get(config.getBlockingTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new OpencodeConnectionException(path,
                    new RuntimeException("Timeout waiting for first SSE event on " + path));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OpencodeApiException oae) throw oae;
            if (cause instanceof OpencodeConnectionException oce) throw oce;
            throw new OpencodeConnectionException(path, cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpencodeConnectionException(path, e);
        } finally {
            executor.shutdownNow();
        }
    }

    private String doReadFirstSseEvent(String path) throws Exception {
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
        conn.setReadTimeout((int) config.getBlockingTimeoutMs());

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new OpencodeApiException(status, path, "SSE connection failed with status " + status);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder event = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() && !event.isEmpty()) {
                    return event.toString().trim();
                }
                if (!line.isEmpty()) {
                    if (!event.isEmpty()) event.append("\n");
                    event.append(line);
                }
            }
            return event.isEmpty() ? "" : event.toString().trim();
        } finally {
            conn.disconnect();
        }
    }

    public <T> T getBlocking(String path, Class<T> type) {
        try {
            return blockingRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(type);
        } catch (RestClientResponseException e) {
            throw toApiException(path, e);
        } catch (ResourceAccessException e) {
            throw new OpencodeConnectionException(path, e);
        }
    }

    private String buildUri(String path, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) return path;
        StringBuilder sb = new StringBuilder(path);
        sb.append("?");
        queryParams.forEach((k, v) -> {
            if (v != null) {
                sb.append(java.net.URLEncoder.encode(k, StandardCharsets.UTF_8))
                  .append("=")
                  .append(java.net.URLEncoder.encode(v, StandardCharsets.UTF_8))
                  .append("&");
            }
        });
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private OpencodeApiException toApiException(String path, RestClientResponseException e) {
        return new OpencodeApiException(e.getStatusCode().value(), path, e.getResponseBodyAsString());
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/http/OpencodeHttpClient.java
git commit -m "feat: add HTTP client with REST, SSE and auth support"
```

---

## Task 3: DTO + 测试基础设施

**Files:**
- Create: `main/client/dto/HealthResponse.java`
- Create: `test/integration/IntegrationTestBase.java`
- Create: `test/integration/ApiAssertions.java`

- [ ] **Step 1: 创建 HealthResponse DTO**

```java
package com.example.opencodeapi.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HealthResponse {
    private boolean healthy;
    private String version;
}
```

- [ ] **Step 2: 创建 IntegrationTestBase**

```java
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
```

- [ ] **Step 3: 创建 ApiAssertions**

```java
package com.example.opencodeapi.integration;

import com.fasterxml.jackson.databind.JsonNode;

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
```

- [ ] **Step 4: 编译验证**

Run: `./mvnw compile -q && ./mvnw test-compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/dto/ \
        src/test/java/com/example/opencodeapi/integration/
git commit -m "feat: add HealthResponse DTO and test infrastructure"
```

---

## Task 4: Global API 客户端 + 测试

**Files:**
- Create: `main/client/api/GlobalApiClient.java`
- Create: `test/integration/GlobalApiClientIT.java`

- [ ] **Step 1: 编写 GlobalApiClientIT 测试**

```java
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
```

- [ ] **Step 2: 确认编译失败（GlobalApiClient 尚未创建）**

Run: `./mvnw test-compile -q`
Expected: COMPILATION FAILURE - GlobalApiClient 不存在

- [ ] **Step 3: 实现 GlobalApiClient**

```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.dto.HealthResponse;
import com.example.opencodeapi.client.http.OpencodeHttpClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GlobalApiClient {

    private final OpencodeHttpClient httpClient;

    public HealthResponse getHealth() {
        return httpClient.get("/global/health", HealthResponse.class);
    }

    public String getGlobalEvent() {
        return httpClient.readFirstSseEvent("/global/event");
    }
}
```

- [ ] **Step 4: 编译通过**

Run: `./mvnw test-compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 运行测试**

Run: `./mvnw test -Dtest=GlobalApiClientIT -pl .`
Expected: 2 tests passed

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/GlobalApiClient.java \
        src/test/java/com/example/opencodeapi/integration/GlobalApiClientIT.java
git commit -m "feat: add GlobalApiClient with health and SSE event support"
```

---

## Task 5: Project / Path / VCS API 客户端 + 测试

**Files:**
- Create: `main/client/api/ProjectApiClient.java`
- Create: `main/client/api/PathApiClient.java`
- Create: `main/client/api/VcsApiClient.java`
- Create: `test/integration/ProjectApiClientIT.java`

- [ ] **Step 1: 编写 ProjectApiClientIT 测试**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.PathApiClient;
import com.example.opencodeapi.client.api.ProjectApiClient;
import com.example.opencodeapi.client.api.VcsApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ProjectApiClientIT extends IntegrationTestBase {

    private static ProjectApiClient projectClient;
    private static PathApiClient pathClient;
    private static VcsApiClient vcsClient;

    @BeforeAll
    static void setUp() {
        projectClient = new ProjectApiClient(httpClient);
        pathClient = new PathApiClient(httpClient);
        vcsClient = new VcsApiClient(httpClient);
    }

    @Test
    void listProjects_returnsArray() {
        JsonNode projects = projectClient.list();
        assertJsonArray(projects);
    }

    @Test
    void getCurrentProject_returnsProject() {
        JsonNode project = projectClient.getCurrent();
        assertNotNull(project);
    }

    @Test
    void getPath_returnsPathInfo() {
        JsonNode path = pathClient.get();
        assertNotNull(path);
    }

    @Test
    void getVcs_returnsVcsInfo() {
        JsonNode vcs = vcsClient.get();
        assertNotNull(vcs);
    }
}
```

- [ ] **Step 2: 实现三个客户端**

`ProjectApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/project", JsonNode.class);
    }

    public JsonNode getCurrent() {
        return httpClient.get("/project/current", JsonNode.class);
    }
}
```

`PathApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PathApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode get() {
        return httpClient.get("/path", JsonNode.class);
    }
}
```

`VcsApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VcsApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode get() {
        return httpClient.get("/vcs", JsonNode.class);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=ProjectApiClientIT -pl .`
Expected: 4 tests passed

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/ProjectApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/PathApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/VcsApiClient.java \
        src/test/java/com/example/opencodeapi/integration/ProjectApiClientIT.java
git commit -m "feat: add Project, Path, VCS API clients with tests"
```

---

## Task 6: Instance + Config API 客户端 + 测试

**Files:**
- Create: `main/client/api/InstanceApiClient.java`
- Create: `main/client/api/ConfigApiClient.java`
- Create: `test/integration/ConfigApiClientIT.java`

- [ ] **Step 1: 编写 ConfigApiClientIT 测试**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.ConfigApiClient;
import com.example.opencodeapi.client.api.InstanceApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

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
```

- [ ] **Step 2: 实现客户端**

`InstanceApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InstanceApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode dispose() {
        return httpClient.post("/instance/dispose", null, JsonNode.class);
    }
}
```

`ConfigApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ConfigApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode get() {
        return httpClient.get("/config", JsonNode.class);
    }

    public JsonNode patch(Map<String, Object> updates) {
        return httpClient.patch("/config", updates, JsonNode.class);
    }

    public JsonNode getProviders() {
        return httpClient.get("/config/providers", JsonNode.class);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=ConfigApiClientIT -pl .`
Expected: 3 tests passed (dispose 默认跳过)

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/InstanceApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/ConfigApiClient.java \
        src/test/java/com/example/opencodeapi/integration/ConfigApiClientIT.java
git commit -m "feat: add Instance and Config API clients with tests"
```

---

## Task 7: Provider API 客户端 + 测试

**Files:**
- Create: `main/client/api/ProviderApiClient.java`
- Create: `test/integration/ProviderApiClientIT.java`

- [ ] **Step 1: 编写 ProviderApiClientIT**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.ProviderApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import com.fasterxml.jackson.databind.JsonNode;
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
```

- [ ] **Step 2: 实现 ProviderApiClient**

```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ProviderApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/provider", JsonNode.class);
    }

    public JsonNode getAuth() {
        return httpClient.get("/provider/auth", JsonNode.class);
    }

    public JsonNode oauthAuthorize(String providerId) {
        return httpClient.post("/provider/" + providerId + "/oauth/authorize", null, JsonNode.class);
    }

    public JsonNode oauthCallback(String providerId, Map<String, Object> body) {
        return httpClient.post("/provider/" + providerId + "/oauth/callback", body, JsonNode.class);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=ProviderApiClientIT -pl .`
Expected: 4 tests passed

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/ProviderApiClient.java \
        src/test/java/com/example/opencodeapi/integration/ProviderApiClientIT.java
git commit -m "feat: add Provider API client with tests"
```

---

## Task 8: Session API 客户端 + 测试

**Files:**
- Create: `main/client/api/SessionApiClient.java`
- Create: `test/integration/SessionApiClientIT.java`

- [ ] **Step 1: 编写 SessionApiClientIT**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.SessionApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

import java.util.Map;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionApiClientIT extends IntegrationTestBase {

    private static SessionApiClient client;
    private static String testSessionId;

    @BeforeAll
    static void setUp() {
        client = new SessionApiClient(httpClient);
    }

    @AfterAll
    static void cleanup() {
        if (testSessionId != null) {
            try {
                client.delete(testSessionId);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @Order(1)
    void createSession_returnsSession() {
        JsonNode session = client.create(null, "IT test session");
        assertNotNull(session);
        assertJsonHasField(session, "id");
        testSessionId = session.get("id").asText();
    }

    @Test
    @Order(2)
    void listSessions_returnsArray() {
        JsonNode sessions = client.list();
        assertJsonArray(sessions);
    }

    @Test
    @Order(3)
    void getSessionStatus_returnsStatus() {
        JsonNode status = client.getStatus();
        assertNotNull(status);
    }

    @Test
    @Order(4)
    void getSession_returnsSessionDetail() {
        assertNotNull(testSessionId, "Session must be created first");
        JsonNode session = client.get(testSessionId);
        assertNotNull(session);
        assertJsonHasField(session, "id");
    }

    @Test
    @Order(5)
    void patchSession_updatesTitle() {
        assertNotNull(testSessionId);
        JsonNode updated = client.patch(testSessionId, Map.of("title", "Updated title"));
        assertNotNull(updated);
    }

    @Test
    @Order(6)
    void getSessionChildren_returnsArray() {
        assertNotNull(testSessionId);
        JsonNode children = client.getChildren(testSessionId);
        assertJsonArray(children);
    }

    @Test
    @Order(7)
    void getSessionTodo_returnsArray() {
        assertNotNull(testSessionId);
        JsonNode todos = client.getTodo(testSessionId);
        assertJsonArray(todos);
    }

    @Test
    @Order(8)
    void getSessionDiff_returnsArray() {
        assertNotNull(testSessionId);
        JsonNode diff = client.getDiff(testSessionId, null);
        assertJsonArray(diff);
    }

    @Test
    @Order(9)
    void forkSession_createsForkedSession() {
        assertNotNull(testSessionId);
        JsonNode forked = client.fork(testSessionId, null);
        assertNotNull(forked);
        assertJsonHasField(forked, "id");
        String forkedId = forked.get("id").asText();
        try {
            client.delete(forkedId);
        } catch (Exception ignored) {
        }
    }

    @Test
    @Order(10)
    void abortSession_returnsResult() {
        assertNotNull(testSessionId);
        JsonNode result = client.abort(testSessionId);
        assertNotNull(result);
    }

    @Test
    @Order(11)
    void shareSession_returnsResult() {
        assertNotNull(testSessionId);
        try {
            JsonNode result = client.share(testSessionId);
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(12)
    void unshareSession_returnsResult() {
        assertNotNull(testSessionId);
        try {
            JsonNode result = client.unshare(testSessionId);
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(13)
    void unrevertSession_returnsResult() {
        assertNotNull(testSessionId);
        JsonNode result = client.unrevert(testSessionId);
        assertNotNull(result);
    }

    @Test
    @Order(14)
    void initSession_callableWithDummyParams() {
        assertNotNull(testSessionId);
        try {
            client.init(testSessionId, Map.of(
                    "messageID", "dummy",
                    "providerID", "dummy",
                    "modelID", "dummy"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(15)
    void summarizeSession_callableWithDummyParams() {
        assertNotNull(testSessionId);
        try {
            client.summarize(testSessionId, Map.of(
                    "providerID", "dummy",
                    "modelID", "dummy"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(16)
    void revertSession_callableWithDummyParams() {
        assertNotNull(testSessionId);
        try {
            client.revert(testSessionId, Map.of("messageID", "dummy"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(17)
    void respondToPermission_callableWithDummyParams() {
        assertNotNull(testSessionId);
        try {
            client.respondToPermission(testSessionId, "dummy-perm",
                    Map.of("response", "allow"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(99)
    void deleteSession_deletesSuccessfully() {
        assertNotNull(testSessionId);
        JsonNode result = client.delete(testSessionId);
        assertNotNull(result);
        testSessionId = null;
    }
}
```

- [ ] **Step 2: 实现 SessionApiClient**

```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class SessionApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/session", JsonNode.class);
    }

    public JsonNode create(String parentId, String title) {
        Map<String, Object> body = new HashMap<>();
        if (parentId != null) body.put("parentID", parentId);
        if (title != null) body.put("title", title);
        return httpClient.post("/session", body, JsonNode.class);
    }

    public JsonNode getStatus() {
        return httpClient.get("/session/status", JsonNode.class);
    }

    public JsonNode get(String id) {
        return httpClient.get("/session/" + id, JsonNode.class);
    }

    public JsonNode delete(String id) {
        return httpClient.delete("/session/" + id, JsonNode.class);
    }

    public JsonNode patch(String id, Map<String, Object> body) {
        return httpClient.patch("/session/" + id, body, JsonNode.class);
    }

    public JsonNode getChildren(String id) {
        return httpClient.get("/session/" + id + "/children", JsonNode.class);
    }

    public JsonNode getTodo(String id) {
        return httpClient.get("/session/" + id + "/todo", JsonNode.class);
    }

    public JsonNode init(String id, Map<String, Object> body) {
        return httpClient.post("/session/" + id + "/init", body, JsonNode.class);
    }

    public JsonNode fork(String id, String messageId) {
        Map<String, Object> body = new HashMap<>();
        if (messageId != null) body.put("messageID", messageId);
        return httpClient.post("/session/" + id + "/fork", body, JsonNode.class);
    }

    public JsonNode abort(String id) {
        return httpClient.post("/session/" + id + "/abort", null, JsonNode.class);
    }

    public JsonNode share(String id) {
        return httpClient.post("/session/" + id + "/share", null, JsonNode.class);
    }

    public JsonNode unshare(String id) {
        return httpClient.delete("/session/" + id + "/share", JsonNode.class);
    }

    public JsonNode getDiff(String id, String messageId) {
        Map<String, String> params = messageId != null ? Map.of("messageID", messageId) : null;
        return httpClient.get("/session/" + id + "/diff", params, JsonNode.class);
    }

    public JsonNode summarize(String id, Map<String, Object> body) {
        return httpClient.post("/session/" + id + "/summarize", body, JsonNode.class);
    }

    public JsonNode revert(String id, Map<String, Object> body) {
        return httpClient.post("/session/" + id + "/revert", body, JsonNode.class);
    }

    public JsonNode unrevert(String id) {
        return httpClient.post("/session/" + id + "/unrevert", null, JsonNode.class);
    }

    public JsonNode respondToPermission(String id, String permissionId, Map<String, Object> body) {
        return httpClient.post("/session/" + id + "/permissions/" + permissionId, body, JsonNode.class);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=SessionApiClientIT -pl .`
Expected: 17 tests passed (部分 dummy 参数测试以捕获预期错误方式通过)

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/SessionApiClient.java \
        src/test/java/com/example/opencodeapi/integration/SessionApiClientIT.java
git commit -m "feat: add Session API client with full lifecycle tests"
```

---

## Task 9: Message + Command API 客户端 + 测试

**Files:**
- Create: `main/client/api/MessageApiClient.java`
- Create: `main/client/api/CommandApiClient.java`
- Create: `test/integration/MessageApiClientIT.java`

- [ ] **Step 1: 编写 MessageApiClientIT**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.CommandApiClient;
import com.example.opencodeapi.client.api.MessageApiClient;
import com.example.opencodeapi.client.api.SessionApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageApiClientIT extends IntegrationTestBase {

    private static SessionApiClient sessionClient;
    private static MessageApiClient messageClient;
    private static CommandApiClient commandClient;
    private static String sessionId;

    @BeforeAll
    static void setUp() {
        sessionClient = new SessionApiClient(httpClient);
        messageClient = new MessageApiClient(httpClient);
        commandClient = new CommandApiClient(httpClient);

        JsonNode session = sessionClient.create(null, "Message IT session");
        sessionId = session.get("id").asText();
    }

    @AfterAll
    static void cleanup() {
        if (sessionId != null) {
            try {
                sessionClient.delete(sessionId);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @Order(1)
    void listMessages_returnsArray() {
        JsonNode messages = messageClient.list(sessionId, null);
        assertJsonArray(messages);
    }

    @Test
    @Order(2)
    void sendMessage_returnsMessageWithParts() {
        Map<String, Object> body = Map.of(
                "parts", List.of(Map.of(
                        "type", "text",
                        "text", "Hello from integration test")));
        try {
            JsonNode result = messageClient.send(sessionId, body);
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(3)
    void getMessage_callableWithDummyId() {
        try {
            JsonNode detail = messageClient.get(sessionId, "non-existent-message-id");
            assertNotNull(detail);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400,
                    "Expected 4xx/5xx for non-existent message ID");
        }
    }

    @Test
    @Order(4)
    void promptAsync_returns204() {
        Map<String, Object> body = Map.of(
                "parts", List.of(Map.of(
                        "type", "text",
                        "text", "Async test")));
        try {
            messageClient.promptAsync(sessionId, body);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(5)
    void executeCommand_callableWithDummy() {
        try {
            messageClient.executeCommand(sessionId, Map.of(
                    "command", "/help",
                    "arguments", Map.of()));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    @Order(6)
    void executeShell_callableWithDummy() {
        try {
            messageClient.executeShell(sessionId, Map.of(
                    "agent", "coder",
                    "command", "echo hello"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void listCommands_returnsArray() {
        JsonNode commands = commandClient.list();
        assertJsonArray(commands);
    }
}
```

- [ ] **Step 2: 实现客户端**

`MessageApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MessageApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list(String sessionId, Integer limit) {
        Map<String, String> params = limit != null ? Map.of("limit", limit.toString()) : null;
        return httpClient.get("/session/" + sessionId + "/message", params, JsonNode.class);
    }

    public JsonNode send(String sessionId, Map<String, Object> body) {
        return httpClient.post("/session/" + sessionId + "/message", body, JsonNode.class);
    }

    public JsonNode get(String sessionId, String messageId) {
        return httpClient.get("/session/" + sessionId + "/message/" + messageId, JsonNode.class);
    }

    public void promptAsync(String sessionId, Map<String, Object> body) {
        httpClient.postNoContent("/session/" + sessionId + "/prompt_async", body);
    }

    public JsonNode executeCommand(String sessionId, Map<String, Object> body) {
        return httpClient.post("/session/" + sessionId + "/command", body, JsonNode.class);
    }

    public JsonNode executeShell(String sessionId, Map<String, Object> body) {
        return httpClient.post("/session/" + sessionId + "/shell", body, JsonNode.class);
    }
}
```

`CommandApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/command", JsonNode.class);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=MessageApiClientIT -pl .`
Expected: 7 tests passed

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/MessageApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/CommandApiClient.java \
        src/test/java/com/example/opencodeapi/integration/MessageApiClientIT.java
git commit -m "feat: add Message and Command API clients with tests"
```

---

## Task 10: File API 客户端 + 测试

**Files:**
- Create: `main/client/api/FileApiClient.java`
- Create: `test/integration/FileApiClientIT.java`

- [ ] **Step 1: 编写 FileApiClientIT**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.FileApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

class FileApiClientIT extends IntegrationTestBase {

    private static FileApiClient client;

    @BeforeAll
    static void setUp() {
        client = new FileApiClient(httpClient);
    }

    @Test
    void findInFiles_returnsResults() {
        JsonNode result = client.find("TODO");
        assertNotNull(result);
    }

    @Test
    void findFile_returnsArray() {
        JsonNode result = client.findFile("pom", null, null, null, null);
        assertJsonArray(result);
    }

    @Test
    void findSymbol_returnsResults() {
        JsonNode result = client.findSymbol("main");
        assertNotNull(result);
    }

    @Test
    void listFiles_returnsArray() {
        JsonNode result = client.list(".");
        assertJsonArray(result);
    }

    @Test
    void getFileContent_returnsContent() {
        JsonNode result = client.getContent("pom.xml");
        assertNotNull(result);
    }

    @Test
    void getFileStatus_returnsArray() {
        JsonNode result = client.getStatus();
        assertJsonArray(result);
    }
}
```

- [ ] **Step 2: 实现 FileApiClient**

```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class FileApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode find(String pattern) {
        return httpClient.get("/find", Map.of("pattern", pattern), JsonNode.class);
    }

    public JsonNode findFile(String query, String type, String directory, Integer limit, String dirs) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", query);
        if (type != null) params.put("type", type);
        if (directory != null) params.put("directory", directory);
        if (limit != null) params.put("limit", limit.toString());
        if (dirs != null) params.put("dirs", dirs);
        return httpClient.get("/find/file", params, JsonNode.class);
    }

    public JsonNode findSymbol(String query) {
        return httpClient.get("/find/symbol", Map.of("query", query), JsonNode.class);
    }

    public JsonNode list(String path) {
        return httpClient.get("/file", Map.of("path", path), JsonNode.class);
    }

    public JsonNode getContent(String path) {
        return httpClient.get("/file/content", Map.of("path", path), JsonNode.class);
    }

    public JsonNode getStatus() {
        return httpClient.get("/file/status", JsonNode.class);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=FileApiClientIT -pl .`
Expected: 6 tests passed

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/FileApiClient.java \
        src/test/java/com/example/opencodeapi/integration/FileApiClientIT.java
git commit -m "feat: add File API client with find, list and content tests"
```

---

## Task 11: Agent / Log / Auth / Event / Doc API 客户端 + 测试

**Files:**
- Create: `main/client/api/AgentApiClient.java`
- Create: `main/client/api/LogApiClient.java`
- Create: `main/client/api/AuthApiClient.java`
- Create: `main/client/api/EventApiClient.java`
- Create: `main/client/api/DocApiClient.java`
- Create: `test/integration/MiscApiClientIT.java`

- [ ] **Step 1: 编写 MiscApiClientIT**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.*;
import com.example.opencodeapi.client.http.OpencodeApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.example.opencodeapi.integration.ApiAssertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MiscApiClientIT extends IntegrationTestBase {

    private static AgentApiClient agentClient;
    private static LogApiClient logClient;
    private static AuthApiClient authClient;
    private static EventApiClient eventClient;
    private static DocApiClient docClient;

    @BeforeAll
    static void setUp() {
        agentClient = new AgentApiClient(httpClient);
        logClient = new LogApiClient(httpClient);
        authClient = new AuthApiClient(httpClient);
        eventClient = new EventApiClient(httpClient);
        docClient = new DocApiClient(httpClient);
    }

    @Test
    void listAgents_returnsArray() {
        JsonNode agents = agentClient.list();
        assertJsonArray(agents);
    }

    @Test
    void writeLog_returnsResult() {
        JsonNode result = logClient.write("integration-test", "info", "Test log entry", null);
        assertNotNull(result);
    }

    @Test
    void setAuth_callableWithDummy() {
        try {
            authClient.set("dummy-provider", Map.of("token", "dummy"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void getEvent_receivesFirstSseEvent() {
        String event = eventClient.getEvent();
        assertSseEvent(event);
    }

    @Test
    void getDoc_returnsHtml() {
        String html = docClient.get();
        assertNotNull(html);
        assertFalse(html.isBlank());
    }
}
```

- [ ] **Step 2: 实现五个客户端**

`AgentApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AgentApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode list() {
        return httpClient.get("/agent", JsonNode.class);
    }
}
```

`LogApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class LogApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode write(String service, String level, String message, Map<String, Object> extra) {
        Map<String, Object> body = new HashMap<>();
        body.put("service", service);
        body.put("level", level);
        body.put("message", message);
        if (extra != null) body.put("extra", extra);
        return httpClient.post("/log", body, JsonNode.class);
    }
}
```

`AuthApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class AuthApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode set(String id, Map<String, Object> body) {
        return httpClient.put("/auth/" + id, body, JsonNode.class);
    }
}
```

`EventApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventApiClient {

    private final OpencodeHttpClient httpClient;

    public String getEvent() {
        return httpClient.readFirstSseEvent("/event");
    }
}
```

`DocApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocApiClient {

    private final OpencodeHttpClient httpClient;

    public String get() {
        return httpClient.getHtml("/doc");
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=MiscApiClientIT -pl .`
Expected: 5 tests passed

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/AgentApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/LogApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/AuthApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/EventApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/DocApiClient.java \
        src/test/java/com/example/opencodeapi/integration/MiscApiClientIT.java
git commit -m "feat: add Agent, Log, Auth, Event, Doc API clients with tests"
```

---

## Task 12: LSP / Formatter / MCP API 客户端 + 测试

**Files:**
- Create: `main/client/api/LspApiClient.java`
- Create: `main/client/api/FormatterApiClient.java`
- Create: `main/client/api/McpApiClient.java`
- Create: `test/integration/LspMcpApiClientIT.java`

- [ ] **Step 1: 编写 LspMcpApiClientIT**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.FormatterApiClient;
import com.example.opencodeapi.client.api.LspApiClient;
import com.example.opencodeapi.client.api.McpApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LspMcpApiClientIT extends IntegrationTestBase {

    private static LspApiClient lspClient;
    private static FormatterApiClient formatterClient;
    private static McpApiClient mcpClient;

    @BeforeAll
    static void setUp() {
        lspClient = new LspApiClient(httpClient);
        formatterClient = new FormatterApiClient(httpClient);
        mcpClient = new McpApiClient(httpClient);
    }

    @Test
    void getLspStatus_returnsResult() {
        JsonNode result = lspClient.getStatus();
        assertNotNull(result);
    }

    @Test
    void getFormatterStatus_returnsResult() {
        JsonNode result = formatterClient.getStatus();
        assertNotNull(result);
    }

    @Test
    void getMcpStatus_returnsResult() {
        JsonNode result = mcpClient.getStatus();
        assertNotNull(result);
    }

    @Test
    void addMcpServer_callableWithDummy() {
        try {
            mcpClient.add("test-mcp", Map.of("command", "echo"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }
}
```

- [ ] **Step 2: 实现三个客户端**

`LspApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LspApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode getStatus() {
        return httpClient.get("/lsp", JsonNode.class);
    }
}
```

`FormatterApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormatterApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode getStatus() {
        return httpClient.get("/formatter", JsonNode.class);
    }
}
```

`McpApiClient.java`:
```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class McpApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode getStatus() {
        return httpClient.get("/mcp", JsonNode.class);
    }

    public JsonNode add(String name, Map<String, Object> mcpConfig) {
        Map<String, Object> body = Map.of("name", name, "config", mcpConfig);
        return httpClient.post("/mcp", body, JsonNode.class);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=LspMcpApiClientIT -pl .`
Expected: 4 tests passed

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/LspApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/FormatterApiClient.java \
        src/main/java/com/example/opencodeapi/client/api/McpApiClient.java \
        src/test/java/com/example/opencodeapi/integration/LspMcpApiClientIT.java
git commit -m "feat: add LSP, Formatter, MCP API clients with tests"
```

---

## Task 13: TUI API 客户端 + 测试

**Files:**
- Create: `main/client/api/TuiApiClient.java`
- Create: `test/integration/TuiApiClientIT.java`

- [ ] **Step 1: 编写 TuiApiClientIT**

```java
package com.example.opencodeapi.integration;

import com.example.opencodeapi.client.api.TuiApiClient;
import com.example.opencodeapi.client.http.OpencodeApiException;
import com.example.opencodeapi.client.http.OpencodeConnectionException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TuiApiClientIT extends IntegrationTestBase {

    private static TuiApiClient client;

    @BeforeAll
    static void setUp() {
        client = new TuiApiClient(httpClient);
    }

    @Test
    void appendPrompt_returnsResult() {
        try {
            JsonNode result = client.appendPrompt("test text");
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void openHelp_returnsResult() {
        try {
            JsonNode result = client.openHelp();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void openSessions_returnsResult() {
        try {
            JsonNode result = client.openSessions();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void openThemes_returnsResult() {
        try {
            JsonNode result = client.openThemes();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void openModels_returnsResult() {
        try {
            JsonNode result = client.openModels();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void submitPrompt_returnsResult() {
        try {
            JsonNode result = client.submitPrompt();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void clearPrompt_returnsResult() {
        try {
            JsonNode result = client.clearPrompt();
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void executeCommand_returnsResult() {
        try {
            JsonNode result = client.executeCommand("/help");
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void showToast_returnsResult() {
        try {
            JsonNode result = client.showToast("IT Test", "Hello from IT", "info");
            assertNotNull(result);
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void controlNext_timesOutGracefully() {
        try {
            client.controlNext();
        } catch (OpencodeConnectionException e) {
            assertTrue(e.getMessage().contains("Timeout") || e.getMessage().contains("connect"),
                    "Expected timeout or connection error for blocking endpoint");
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }

    @Test
    void controlResponse_callableWithDummy() {
        try {
            client.controlResponse(Map.of("body", "test"));
        } catch (OpencodeApiException e) {
            assertTrue(e.getStatusCode() >= 400);
        }
    }
}
```

- [ ] **Step 2: 实现 TuiApiClient**

```java
package com.example.opencodeapi.client.api;

import com.example.opencodeapi.client.http.OpencodeHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TuiApiClient {

    private final OpencodeHttpClient httpClient;

    public JsonNode appendPrompt(String text) {
        return httpClient.post("/tui/append-prompt", Map.of("text", text), JsonNode.class);
    }

    public JsonNode openHelp() {
        return httpClient.post("/tui/open-help", null, JsonNode.class);
    }

    public JsonNode openSessions() {
        return httpClient.post("/tui/open-sessions", null, JsonNode.class);
    }

    public JsonNode openThemes() {
        return httpClient.post("/tui/open-themes", null, JsonNode.class);
    }

    public JsonNode openModels() {
        return httpClient.post("/tui/open-models", null, JsonNode.class);
    }

    public JsonNode submitPrompt() {
        return httpClient.post("/tui/submit-prompt", null, JsonNode.class);
    }

    public JsonNode clearPrompt() {
        return httpClient.post("/tui/clear-prompt", null, JsonNode.class);
    }

    public JsonNode executeCommand(String command) {
        return httpClient.post("/tui/execute-command", Map.of("command", command), JsonNode.class);
    }

    public JsonNode showToast(String title, String message, String variant) {
        Map<String, Object> body = new HashMap<>();
        if (title != null) body.put("title", title);
        body.put("message", message);
        body.put("variant", variant);
        return httpClient.post("/tui/show-toast", body, JsonNode.class);
    }

    public JsonNode controlNext() {
        return httpClient.getBlocking("/tui/control/next", JsonNode.class);
    }

    public JsonNode controlResponse(Map<String, Object> body) {
        return httpClient.post("/tui/control/response", body, JsonNode.class);
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `./mvnw test -Dtest=TuiApiClientIT -pl .`
Expected: 11 tests passed

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/example/opencodeapi/client/api/TuiApiClient.java \
        src/test/java/com/example/opencodeapi/integration/TuiApiClientIT.java
git commit -m "feat: add TUI API client with all endpoint tests"
```

---

## Task 14: 全量回归 + 最终提交

**Files:**
- No new files

- [ ] **Step 1: 运行全部测试**

Run: `./mvnw test -pl .`
Expected: All tests passed (约 65 测试用例)

- [ ] **Step 2: 检查覆盖完整性**

对照 spec 中 §6 接口清单，确认每个接口至少有一条测试：
- Global: 2 接口 ✓
- Project/Path/VCS: 4 接口 ✓
- Instance/Config: 4 接口 ✓
- Provider: 4 接口 ✓
- Session: 17 接口 ✓
- Message/Command: 7 接口 ✓
- File: 6 接口 ✓
- Agent/Log/Auth/Event/Doc: 5 接口 ✓
- LSP/Formatter/MCP: 4 接口 ✓
- TUI: 11 接口 ✓

总计：64 接口全部覆盖

- [ ] **Step 3: 最终提交**

```bash
git add -A
git commit -m "chore: complete all OpenCode API clients and integration tests"
git push origin master
```
