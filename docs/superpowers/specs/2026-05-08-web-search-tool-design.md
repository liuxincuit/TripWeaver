# Web Search 工具集成设计

## 概述

为 TripWeaver 添加 Web Search 工具，让 AI 能够根据用户需求自动搜索实时信息（如交通班次、票价、酒店价格等），提升旅行规划的准确性。

## 需求

- AI 自动判断何时需要调用搜索工具
- 使用 SearXNG 作为搜索引擎
- 搜索失败时自动重试，全部失败后告知用户
- 配置通过环境变量提供

## 架构

```
┌─────────────────────────────────────────────────────┐
│                    AiService                         │
│                                                      │
│              基础提示词（旅行规划指南）               │
│                       │                              │
│                  ChatClient                          │
│                       │                              │
│           Spring AI Function Calling                 │
│           （自动注入工具定义到请求）                  │
│                       │                              │
│                       ▼                              │
│                WebSearchTool                         │
│                 @Tool 注解                           │
│                       │                              │
│                       ▼                              │
│              SearXNGClient                           │
│              (重试 + 降级)                            │
└─────────────────────────────────────────────────────┘
```

## 组件设计

### 1. WebSearchTool

使用 Spring AI 的 `@Tool` 注解定义工具，模型根据描述自动判断调用时机。

```java
@Component
public class WebSearchTool {

    private final SearXNGClient searxngClient;

    @Tool(description = """
        搜索互联网获取实时信息，如交通班次、票价、景点开放时间、酒店价格等。
        当用户询问需要实时数据的问题时使用此工具。
        """)
    public String search(@ToolParam(description = "搜索查询关键词") String query) {
        return searxngClient.search(query);
    }
}
```

### 2. SearXNGClient

负责与 SearXNG 通信，包含重试逻辑。

```java
@Component
public class SearXNGClient {

    private final SearXNGProperties properties;
    private final WebClient webClient;

    public String search(String query) {
        // 重试逻辑
        // 失败时返回错误信息
    }
}
```

### 3. SearXNGProperties

配置属性类，所有配置从外部读取。

```java
@ConfigurationProperties(prefix = "searxng")
@Validated
public class SearXNGProperties {

    @NotBlank(message = "SearXNG base URL is required")
    private String baseUrl;

    private int retryCount;

    private int retryDelayMs;
}
```

### 4. AiService 改造

```java
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final WebSearchTool webSearchTool;

    private static final String BASE_SYSTEM_PROMPT = """
        你是一个专业的旅行规划助手...
        """;

    public String chat(String userMessage) {
        return chatClient.prompt()
                .system(BASE_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(webSearchTool)
                .call()
                .content();
    }
}
```

## 配置

### application.yml

```yaml
searxng:
  base-url: ${SEARXNG_BASE_URL}
  retry-count: 3
  retry-delay-ms: 1000
```

### 环境变量

```bash
SEARXNG_BASE_URL=http://172.27.16.134:7000
```

## 重试与降级策略

```
请求 → 失败 → 重试(1) → 失败 → 重试(2) → 失败 → 重试(3) → 失败
                                                          │
                                                          ▼
                                              返回错误信息给模型
                                              模型告知用户搜索服务不可用
```

- 重试次数：可配置，默认 3 次
- 重试间隔：可配置，默认 1000ms
- 最终失败：返回明确错误信息，由模型告知用户

## 文件变更

### 新增文件

| 文件 | 说明 |
|------|------|
| `config/SearXNGProperties.java` | 配置属性类 |
| `client/SearXNGClient.java` | SearXNG HTTP 客户端 |
| `tools/WebSearchTool.java` | Web 搜索工具 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `ai/AiService.java` | 添加工具注册 |
| `application.yml` | 添加 searxng 配置 |

### 删除文件

| 文件 | 原因 |
|------|------|
| `tools/WeatherTool.java` | 被 WebSearchTool 替代 |
| `tools/ToolConfig.java` | 不再需要手动注册工具 |

## 测试策略

1. **单元测试**
   - `WebSearchTool` 正常调用
   - `SearXNGClient` 重试逻辑
   - 配置属性校验

2. **集成测试**
   - 使用 WireMock 模拟 SearXNG 响应
   - 测试 AI 自动调用工具的场景
   - 测试搜索失败的降级处理
