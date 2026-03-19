# Controller Layer Design for OpenCode API

**Date:** 2026-03-19
**Status:** Approved

## Overview

为 OpenCode API 客户端库增加 Controller 层，将其从一个纯客户端库转变为一个代理服务/网关，对外暴露 REST API，内部通过已有的 API Client 转发到 OpenCode Server。

## Architecture

```
客户端请求 → Controller → Service → API Client → OpencodeHttpClient → OpenCode Server
```

采用一对一直接映射：每个 API Client 对应一个 Service 和一个 Controller。

## 1. Unified Response Wrapper

### `ApiResponse<T>`

位置：`com.example.opencodeapi.dto.ApiResponse`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;        // HTTP 状态码
    private String message;  // 描述信息
    private T data;          // 业务数据
}
```

- 所有 Controller 统一返回 `ResponseEntity<ApiResponse<?>>`
- 成功时 `code=200`，`data` 为 `JsonNode` 或强类型 DTO
- 特殊情况：`DocController` 返回 HTML，不包裹 `ApiResponse`；`promptAsync` 无返回体，`data` 为 `null`

提供静态工厂方法：
- `ApiResponse.ok(T data)` — 成功响应
- `ApiResponse.error(int code, String message)` — 错误响应

## 2. Global Exception Handler

位置：`com.example.opencodeapi.exception.GlobalExceptionHandler`

使用 `@RestControllerAdvice` 实现全局异常处理：

| 异常类型 | HTTP 状态码 | 说明 |
|---------|-----------|------|
| `OpencodeApiException` | 透传原始状态码 | OpenCode Server 返回的业务错误 |
| `OpencodeConnectionException` | 502 Bad Gateway | OpenCode Server 不可达 |
| `IllegalArgumentException` | 400 Bad Request | 参数校验失败 |
| `Exception`（兜底） | 500 Internal Server Error | 未预期的异常，记录日志 |

个别 Controller 方法可在方法内 try-catch 做特殊处理（如 `TuiController.controlNext` 的超时处理）。

## 3. Spring Bean Configuration

位置：`com.example.opencodeapi.config.OpencodeBeansConfig`

### 设计原则

- **不修改现有 client 类**，通过 `@Configuration` + `@Bean` 注册
- 配置优先级：`application.properties` > 环境变量（兼容两种配置方式）

### 配置映射

| application.properties | 环境变量 | 默认值 |
|-----------------------|---------|-------|
| `opencode.base-url` | `OPENCODE_BASE_URL` | （必填） |
| `opencode.server.username` | `OPENCODE_SERVER_USERNAME` | `opencode` |
| `opencode.server.password` | `OPENCODE_SERVER_PASSWORD` | （可选） |
| `opencode.timeout-ms` | `OPENCODE_TIMEOUT_MS` | `10000` |
| `opencode.blocking-timeout-ms` | `OPENCODE_BLOCKING_TIMEOUT_MS` | `3000` |

### 注册的 Bean

- `OpencodeClientConfig`
- `OpencodeHttpClient`
- 20 个 API Client（`GlobalApiClient`、`SessionApiClient` 等）

## 4. Service Layer

位置：`com.example.opencodeapi.service.*`

每个 API Client 对应一个 `@Service`，职责：
- 调用对应的 API Client 方法
- 简单的参数组装
- 为后续扩展预留空间（缓存、限流、审计日志等）

### Service 列表

| Service | 注入的 API Client | 方法数 |
|---------|------------------|-------|
| `GlobalService` | GlobalApiClient | 2 |
| `ProjectService` | ProjectApiClient | 2 |
| `PathService` | PathApiClient | 1 |
| `VcsService` | VcsApiClient | 1 |
| `InstanceService` | InstanceApiClient | 1 |
| `ConfigService` | ConfigApiClient | 3 |
| `ProviderService` | ProviderApiClient | 4 |
| `SessionService` | SessionApiClient | 17 |
| `MessageService` | MessageApiClient | 6 |
| `CommandService` | CommandApiClient | 1 |
| `FileService` | FileApiClient | 6 |
| `AgentService` | AgentApiClient | 1 |
| `LogService` | LogApiClient | 1 |
| `AuthService` | AuthApiClient | 1 |
| `EventService` | EventApiClient | 1 |
| `DocService` | DocApiClient | 1 |
| `LspService` | LspApiClient | 1 |
| `FormatterService` | FormatterApiClient | 1 |
| `McpService` | McpApiClient | 2 |
| `TuiService` | TuiApiClient | 11 |

**总计 20 个 Service，64 个方法。**

### SSE Service 特殊处理

`GlobalService` 和 `EventService` 中的 SSE 方法返回 `SseEmitter`，使用独立线程池管理 SSE 连接。

## 5. SSE Proxy Design

两个 SSE 端点需要使用 `SseEmitter` 做持续事件流转发：

- `GET /api/v1/global/event`
- `GET /api/v1/event`

### 实现方式

1. Service 层创建 `SseEmitter` 对象（可配置超时，默认 30 秒）
2. 使用独立线程池（`ExecutorService`）打开到 OpenCode Server 的 SSE 连接
3. 逐行读取 SSE 事件，通过 `emitter.send()` 推送给客户端
4. 连接断开或超时时自动清理资源
5. SSE 通用逻辑提取到 Service 基类或工具方法中复用

### Controller 端

SSE 端点直接返回 `SseEmitter`（不包裹 `ApiResponse`），客户端通过 `timeout` 查询参数控制超时时间。

## 6. Controller Mapping

所有 Controller 使用 `/api/v1/` 统一前缀。

### GlobalController — `/api/v1/global`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/health` | GET | 健康检查 |
| `/event` | GET (SSE) | 全局事件流 |

### ProjectController — `/api/v1/project`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | 项目列表 |
| `/current` | GET | 当前项目 |

### PathController — `/api/v1/path`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | 获取路径 |

### VcsController — `/api/v1/vcs`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | 获取 VCS 信息 |

### InstanceController — `/api/v1/instance`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/dispose` | POST | 销毁实例 |

### ConfigController — `/api/v1/config`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | 获取配置 |
| `/` | PATCH | 更新配置 |
| `/providers` | GET | 获取 Providers 配置 |

### ProviderController — `/api/v1/provider`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | Provider 列表 |
| `/auth` | GET | 认证信息 |
| `/{providerId}/oauth/authorize` | POST | OAuth 授权 |
| `/{providerId}/oauth/callback` | POST | OAuth 回调 |

### SessionController — `/api/v1/session`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | Session 列表 |
| `/` | POST | 创建 Session |
| `/status` | GET | Session 状态 |
| `/{id}` | GET | 获取 Session |
| `/{id}` | PATCH | 更新 Session |
| `/{id}` | DELETE | 删除 Session |
| `/{id}/children` | GET | 子 Session |
| `/{id}/todo` | GET | Todo |
| `/{id}/init` | POST | 初始化 |
| `/{id}/fork` | POST | Fork |
| `/{id}/abort` | POST | 中止 |
| `/{id}/share` | POST | 分享 |
| `/{id}/share` | DELETE | 取消分享 |
| `/{id}/diff` | GET | Diff |
| `/{id}/summarize` | POST | 摘要 |
| `/{id}/revert` | POST | 回退 |
| `/{id}/unrevert` | POST | 取消回退 |
| `/{id}/permissions/{permissionId}` | POST | 权限响应 |

### MessageController — `/api/v1/session/{sessionId}/message`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | 消息列表 |
| `/` | POST | 发送消息 |
| `/{messageId}` | GET | 获取消息 |
| `../prompt_async` | POST | 异步 Prompt（无返回体，HTTP 204） |
| `../command` | POST | 执行命令 |
| `../shell` | POST | 执行 Shell |

### CommandController — `/api/v1/command`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | 命令列表 |

### FileController — `/api/v1/find` + `/api/v1/file`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/api/v1/find` | GET | 文件查找（pattern） |
| `/api/v1/find/file` | GET | 文件搜索 |
| `/api/v1/find/symbol` | GET | 符号搜索 |
| `/api/v1/file` | GET | 文件列表 |
| `/api/v1/file/content` | GET | 文件内容 |
| `/api/v1/file/status` | GET | 文件状态 |

### AgentController — `/api/v1/agent`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | Agent 列表 |

### LogController — `/api/v1/log`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | POST | 写入日志 |

### AuthController — `/api/v1/auth`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/{id}` | PUT | 设置认证 |

### EventController — `/api/v1/event`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET (SSE) | 事件流 |

### DocController — `/api/v1/doc`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | 获取文档（返回 HTML，不包裹 ApiResponse） |

### LspController — `/api/v1/lsp`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | LSP 状态 |

### FormatterController — `/api/v1/formatter`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | Formatter 状态 |

### McpController — `/api/v1/mcp`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/` | GET | MCP 状态 |
| `/` | POST | 添加 MCP |

### TuiController — `/api/v1/tui`

| 端点 | HTTP 方法 | 说明 |
|------|----------|------|
| `/append-prompt` | POST | 追加 Prompt |
| `/open-help` | POST | 打开帮助 |
| `/open-sessions` | POST | 打开 Sessions |
| `/open-themes` | POST | 打开主题 |
| `/open-models` | POST | 打开模型 |
| `/submit-prompt` | POST | 提交 Prompt |
| `/clear-prompt` | POST | 清除 Prompt |
| `/execute-command` | POST | 执行命令 |
| `/show-toast` | POST | 显示 Toast |
| `/control/next` | GET | 获取下一个控制事件（阻塞短超时） |
| `/control/response` | POST | 控制响应 |

**总计 20 个 Controller，64 个端点。**

## 7. Testing Strategy

### 7.1 Unit Tests (`@WebMvcTest`)

位置：`src/test/java/com/example/opencodeapi/controller/`

每个 Controller 对应一个 Test 类，Mock 掉 Service 层。覆盖：
- 正常返回场景（验证状态码、响应结构、`ApiResponse` 包装）
- 异常场景（`OpencodeApiException` → 对应状态码，`OpencodeConnectionException` → 502）
- 参数绑定（路径参数、查询参数、请求体）
- SSE 端点的 `SseEmitter` 返回

### 7.2 Integration Tests (`@SpringBootTest`)

位置：`src/test/java/com/example/opencodeapi/integration/`

使用 `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`，验证完整链路。复用现有 `IntegrationTestBase` 基础设施。

### Test Class List

| 单元测试 | 集成测试 |
|---------|---------|
| `GlobalControllerTest` | `GlobalControllerIT` |
| `ProjectControllerTest` | `ProjectControllerIT` |
| `PathControllerTest` | `PathControllerIT` |
| `VcsControllerTest` | `VcsControllerIT` |
| `InstanceControllerTest` | `InstanceControllerIT` |
| `ConfigControllerTest` | `ConfigControllerIT` |
| `ProviderControllerTest` | `ProviderControllerIT` |
| `SessionControllerTest` | `SessionControllerIT` |
| `MessageControllerTest` | `MessageControllerIT` |
| `CommandControllerTest` | `CommandControllerIT` |
| `FileControllerTest` | `FileControllerIT` |
| `AgentControllerTest` | `AgentControllerIT` |
| `LogControllerTest` | `LogControllerIT` |
| `AuthControllerTest` | `AuthControllerIT` |
| `EventControllerTest` | `EventControllerIT` |
| `DocControllerTest` | `DocControllerIT` |
| `LspControllerTest` | `LspControllerIT` |
| `FormatterControllerTest` | `FormatterControllerIT` |
| `McpControllerTest` | `McpControllerIT` |
| `TuiControllerTest` | `TuiControllerIT` |

## 8. New Files Summary

| 类别 | 数量 | 位置 |
|------|------|------|
| Config | 1 | `config/OpencodeBeansConfig.java` |
| DTO | 1 | `dto/ApiResponse.java` |
| Exception | 1 | `exception/GlobalExceptionHandler.java` |
| Controller | 20 | `controller/*.java` |
| Service | 20 | `service/*.java` |
| Unit Test | 20 | `test/.../controller/*Test.java` |
| Integration Test | 20 | `test/.../integration/*ControllerIT.java` |
| **Total** | **83** | |

## 9. Design Principles

- **不修改现有代码**：所有 client 层代码和已有测试保持不变
- **一对一映射**：每个 API Client ↔ Service ↔ Controller 清晰对应
- **统一响应格式**：除 HTML 和 SSE 外，所有端点返回 `ApiResponse<T>`
- **全局异常兜底**：`@RestControllerAdvice` 统一处理，个别特殊接口可局部覆盖
- **配置兼容**：同时支持 `application.properties` 和环境变量
- **遵循项目规范**：使用 Lombok、构造器注入、K&R 缩进风格等
