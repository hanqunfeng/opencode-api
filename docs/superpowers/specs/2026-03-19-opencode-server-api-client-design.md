# OpenCode Server API 客户端与测试设计

日期：2026-03-19  
项目：`opencode-api`  
范围：核心 API + TUI + LSP/Formatter/MCP（不包含 experimental/tool/*）

## 1. 背景与目标

基于 OpenCode 服务器 API 文档，实现 Java 客户端对目标范围内全部接口的调用能力，并为每个接口提供至少一条集成测试用例。

约束与约定：
- 测试方式：集成测试优先。
- 服务启动：由使用者手动启动 `opencode serve`。
- 认证模式：无认证与 Basic Auth 两种都要支持，通过环境变量切换。
- 参考文档：<https://opencode.ai/docs/zh-cn/server/>

## 2. 方案选择与结论

已比较三种方案：
1) 分组强类型客户端 + 集成测试矩阵  
2) 通用 HTTP Executor + 薄封装  
3) OpenAPI 生成 + 手工补丁

最终选择方案 1（分组强类型客户端 + 集成测试矩阵），理由：
- 大规模接口覆盖下可维护性更高；
- 接口语义清晰，便于定位问题；
- 后续扩展成本低，测试组织稳定。

## 3. 架构设计

### 3.1 配置层

`OpencodeClientConfig` 统一读取并管理：
- `OPENCODE_BASE_URL`（例如 `http://127.0.0.1:4096`）
- `OPENCODE_SERVER_USERNAME`（可选）
- `OPENCODE_SERVER_PASSWORD`（可选）
- `OPENCODE_TIMEOUT_MS`（可选，普通请求超时，默认 `10000`）
- `OPENCODE_BLOCKING_TIMEOUT_MS`（可选，阻塞接口超时，默认 `3000`）

说明：
- 客户端侧环境变量使用 `OPENCODE_SERVER_USERNAME`/`OPENCODE_SERVER_PASSWORD` 发送 Basic Auth；
- 服务端认证由 `OPENCODE_SERVER_USERNAME`/`OPENCODE_SERVER_PASSWORD` 控制，二者相互独立。
- `OPENCODE_BASE_URL` 未设置或为空时，客户端应在初始化阶段 fail-fast 并抛出明确错误，提示先设置该变量并确认 `opencode serve` 已启动。

认证策略：
- 若 `OPENCODE_SERVER_PASSWORD` 为空：不附加 Basic Auth。
- 若 `OPENCODE_SERVER_PASSWORD` 非空：附加 Basic Auth，用户名默认取 `OPENCODE_SERVER_USERNAME`，为空时回退 `opencode`。

### 3.2 传输层

`OpencodeHttpClient` 负责：
- 统一发起 GET/POST/PATCH/PUT/DELETE；
- 拼接 query 参数；
- JSON 序列化/反序列化；
- 统一状态码与异常处理；
- SSE 首事件读取能力（用于 `/event` 与 `/global/event`）。

### 3.3 API 分组客户端

按文档分组创建客户端类，每个接口映射为一个方法：
- `GlobalApiClient`
- `InstanceApiClient`
- `ProjectApiClient`
- `PathApiClient`
- `VcsApiClient`
- `ConfigApiClient`
- `ProviderApiClient`
- `SessionApiClient`
- `MessageApiClient`
- `CommandApiClient`
- `FileApiClient`
- `AgentApiClient`
- `LogApiClient`
- `AuthApiClient`
- `EventApiClient`
- `DocApiClient`
- `LspApiClient`
- `FormatterApiClient`
- `McpApiClient`
- `TuiApiClient`

数据建模策略：
- 结构稳定响应：使用 DTO（例如健康检查响应）；
- 结构动态或模型相关响应：使用 `JsonNode` 兜底，减少脆弱建模。

### 3.4 异常模型

- `OpencodeApiException`：包含 `statusCode`、`path`、`responseBodySnippet`。
- `OpencodeConnectionException`：用于服务不可达、超时等连接层异常，错误信息应明确提示先启动 `opencode serve`。

### 3.5 Instance 生命周期约定

- 本设计不负责“创建实例”，默认依赖 `opencode serve` 启动后自动提供当前实例；
- `POST /instance/dispose` 语义为销毁当前实例，调用后可能影响同一服务上的其他请求；
- 客户端不实现多实例选择逻辑，统一面向服务端当前实例工作；
- 因此 `instance/dispose` 仅作为受控测试能力，不纳入默认回归链路。

## 4. 数据流设计

调用链：
1. 业务或测试调用 `XxxApiClient` 方法；
2. `XxxApiClient` 只处理路径、参数和请求体；
3. 统一委托 `OpencodeHttpClient` 执行；
4. 返回 DTO/基础类型/`JsonNode`；
5. 非 2xx 或连接异常抛出统一异常。

SSE：
- 提供 `readFirstEvent(timeout)` 能力；
- 测试仅验证“可连接并收到事件格式内容”，避免引入长连接不稳定因素。
- 对阻塞接口（如 `/tui/control/next`）设置单独超时配置 `OPENCODE_BLOCKING_TIMEOUT_MS`，默认建议 3000ms。

## 5. 测试设计

### 5.1 测试方式

集成测试优先，依赖手动启动的 `opencode serve`。

建议启动命令：
```bash
opencode serve --hostname 127.0.0.1 --port 4096
```

CI 策略：
- CI 环境默认也走集成测试，但需要在流水线前置步骤中启动 `opencode serve`；
- 若当前流水线不具备该依赖，可通过环境变量 `SKIP_OPENCODE_IT=true` 跳过集成测试任务（由 Maven profile 或 surefire 配置读取）。

### 5.2 测试环境变量

- `OPENCODE_BASE_URL`（必填）
- `OPENCODE_SERVER_USERNAME`（可选）
- `OPENCODE_SERVER_PASSWORD`（可选）
- `OPENCODE_TIMEOUT_MS`（可选，默认 `10000`）
- `OPENCODE_BLOCKING_TIMEOUT_MS`（可选，默认 `3000`）

覆盖策略：
- 无认证场景：仅设置 `OPENCODE_BASE_URL`；
- Basic Auth 场景：额外设置用户名/密码；
- 同一测试代码，通过环境变量驱动。

### 5.3 测试组织

新增测试基类与断言辅助：
- `IntegrationTestBase`：环境读取、健康检查预检、统一客户端初始化；
- `ApiAssertions`：通用断言逻辑。

每个 API 分组一个 `*IT` 类；每个接口至少一条用例。  
有状态接口尽量采用“创建 -> 查询 -> 清理”闭环，避免污染。

副作用接口隔离：
- `POST /instance/dispose` 仅在专用 profile（如 `-Pit-destructive`）或显式开关 `ENABLE_DESTRUCTIVE_IT=true` 下执行；
- 默认测试流程不执行该接口，避免中断其余集成测试会话。

## 6. 接口覆盖清单（核心 + TUI + LSP/Formatter/MCP）

范围定义：
- 本章节覆盖“核心 API + TUI + LSP/Formatter/MCP”，不包含 `experimental/tool/*`。

### 6.1 Global
- `GET /global/health`
- `GET /global/event`（SSE）

### 6.2 Project / Path / VCS
- `GET /project`
- `GET /project/current`
- `GET /path`
- `GET /vcs`

### 6.3 Instance / Config / Provider
- `POST /instance/dispose`（单独测试并谨慎执行）
- `GET /config`
- `PATCH /config`
- `GET /config/providers`
- `GET /provider`
- `GET /provider/auth`
- `POST /provider/{id}/oauth/authorize`（可调用性验证）
- `POST /provider/{id}/oauth/callback`（可调用性验证）

### 6.4 Session
- `GET /session`
- `POST /session`
- `GET /session/status`
- `GET /session/:id`
- `PATCH /session/:id`
- `GET /session/:id/children`
- `GET /session/:id/todo`
- `POST /session/:id/fork`
- `POST /session/:id/abort`
- `POST /session/:id/share`
- `DELETE /session/:id/share`
- `GET /session/:id/diff`
- `POST /session/:id/init`（请求体：`{ messageID, providerID, modelID }`）
- `POST /session/:id/unrevert`（恢复全部已回退消息，通常与 `revert` 成对测试）
- `POST /session/:id/summarize`（请求体：`{ providerID, modelID }`）
- `POST /session/:id/revert`（请求体：`{ messageID, partID? }`）
- `POST /session/:id/permissions/:permissionID`（请求体：`{ response, remember? }`）
- `DELETE /session/:id`

### 6.5 Message / Command
- `GET /session/:id/message`
- `POST /session/:id/message`
- `GET /session/:id/message/:messageID`
- `POST /session/:id/prompt_async`
- `POST /session/:id/command`
- `POST /session/:id/shell`
- `GET /command`

### 6.6 File / Agent / Log / Auth / Event / Doc
- `GET /find?pattern=...`
- `GET /find/file?query=...`（可选参数：`type`、`directory`、`limit`、`dirs`）
- `GET /find/symbol?query=...`
- `GET /file?path=...`
- `GET /file/content?path=...`
- `GET /file/status`
- `GET /agent`
- `POST /log`
- `PUT /auth/:id`
- `GET /event`（SSE）
- `GET /doc`

### 6.7 LSP / Formatter / MCP
- `GET /lsp`
- `GET /formatter`
- `GET /mcp`
- `POST /mcp`（请求体：`{ name, config }`）

### 6.8 TUI
- `POST /tui/append-prompt`
- `POST /tui/open-help`
- `POST /tui/open-sessions`
- `POST /tui/open-themes`
- `POST /tui/open-models`
- `POST /tui/submit-prompt`
- `POST /tui/clear-prompt`
- `POST /tui/execute-command`（请求体：`{ command }`）
- `POST /tui/show-toast`（请求体：`{ title?, message, variant }`）
- `GET /tui/control/next`（阻塞式接口，需要单独超时策略）
- `POST /tui/control/response`

## 7. 实施阶段与验收标准

### 7.1 实施阶段

1. 基础设施：配置、HTTP 执行器、异常模型、认证切换  
2. 核心 API：先完成稳定接口组与用例  
3. 会话消息链路：实现状态相关接口和闭环测试  
4. TUI + LSP/Formatter/MCP：补齐范围 B 并完善覆盖

### 7.2 验收标准（DoD）

- 覆盖范围与本设计一致，不遗漏接口；
- 每个接口至少 1 条测试用例；
- 测试可重复执行，副作用接口有清理策略；
- 无认证与 Basic Auth 两种模式都可跑通；
- 失败日志包含足够诊断信息（状态码、路径、响应摘要）。

## 8. 风险与缓解

- 外部依赖风险：`opencode serve` 未启动导致测试失败。  
  缓解：测试启动时先访问 `/global/health`，并在错误信息中给出启动指引。

- 动态响应结构风险：部分接口响应可能随版本变化。  
  缓解：以 `JsonNode` 兜底，测试优先断言关键字段和状态。

- SSE 稳定性风险：长连接测试易波动。  
  缓解：仅验证连接建立和首条事件读取。

- 阻塞接口风险：`/tui/control/next` 可能长时间等待。  
  缓解：使用阻塞接口专用超时参数并在测试中限制等待时间。

## 9. 参考

- OpenCode 文档：<https://opencode.ai/docs/zh-cn/server/>
