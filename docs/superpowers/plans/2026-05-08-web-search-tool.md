# Web Search 工具集成实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 TripWeaver 添加 Web Search 工具，让 AI 能够自动搜索实时信息。

**Architecture:** 使用 Spring AI Function Calling 机制，通过 `@Tool` 注解定义工具，模型自动判断调用时机。SearXNG 作为搜索引擎，配置通过环境变量提供。

**Tech Stack:** Java 21, Spring Boot 3.5.14, Spring AI 1.1.5, SearXNG

---

## 文件结构

```
backend/src/main/java/com/tripweaver/
├── config/
│   └── SearXNGProperties.java    # 新增：配置属性类
├── client/
│   └── SearXNGClient.java        # 新增：SearXNG HTTP 客户端
├── tools/
│   ├── WebSearchTool.java        # 新增：Web 搜索工具
│   ├── WeatherTool.java          # 删除
│   └── ToolConfig.java           # 删除
├── ai/
│   └── AiService.java            # 修改：添加工具注册

backend/src/test/java/com/tripweaver/
├── client/
│   └── SearXNGClientTest.java    # 新增：客户端测试
├── tools/
│   └── WebSearchToolTest.java    # 新增：工具测试

backend/src/main/resources/
└── application.yml               # 修改：添加 searxng 配置

.env.example                      # 修改：添加 SEARXNG_BASE_URL
README.md                         # 修改：更新配置说明
CLAUDE.md                         # 检查：确认无需修改
```

---

### Task 1: SearXNG 配置属性类

**Files:**
- Create: `backend/src/main/java/com/tripweaver/config/SearXNGProperties.java`
- Test: `backend/src/test/java/com/tripweaver/config/SearXNGPropertiesTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.tripweaver.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearXNGPropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldFailWhenBaseUrlIsBlank() {
        SearXNGProperties properties = new SearXNGProperties();
        properties.setBaseUrl("");
        properties.setRetryCount(3);
        properties.setRetryDelayMs(1000);

        Set<ConstraintViolation<SearXNGProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("SearXNG base URL is required")));
    }

    @Test
    void shouldPassWhenBaseUrlIsValid() {
        SearXNGProperties properties = new SearXNGProperties();
        properties.setBaseUrl("http://localhost:8080");
        properties.setRetryCount(3);
        properties.setRetryDelayMs(1000);

        Set<ConstraintViolation<SearXNGProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=SearXNGPropertiesTest`
Expected: FAIL - class not found

- [ ] **Step 3: Write minimal implementation**

```java
package com.tripweaver.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "searxng")
public class SearXNGProperties {

    @NotBlank(message = "SearXNG base URL is required")
    private String baseUrl;

    private int retryCount;

    private int retryDelayMs;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=SearXNGPropertiesTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/tripweaver/config/SearXNGProperties.java backend/src/test/java/com/tripweaver/config/SearXNGPropertiesTest.java
git commit -m "feat: add SearXNG configuration properties"
```

---

### Task 2: SearXNG HTTP 客户端

**Files:**
- Create: `backend/src/main/java/com/tripweaver/client/SearXNGClient.java`
- Test: `backend/src/test/java/com/tripweaver/client/SearXNGClientTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.tripweaver.client;

import com.tripweaver.config.SearXNGProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SearXNGClientTest {

    private MockWebServer mockWebServer;
    private SearXNGClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        SearXNGProperties properties = new SearXNGProperties();
        properties.setBaseUrl(mockWebServer.url("/").toString());
        properties.setRetryCount(2);
        properties.setRetryDelayMs(100);

        client = new SearXNGClient(properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnSearchResults() {
        String mockResponse = """
            {
              "results": [
                {"title": "北京到上海高铁", "url": "https://example.com/1", "content": "高铁班次信息..."}
              ]
            }
            """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        String result = client.search("北京到上海高铁");

        assertNotNull(result);
        assertTrue(result.contains("北京到上海高铁"));
    }

    @Test
    void shouldRetryOnFailure() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"results\": []}")
                .addHeader("Content-Type", "application/json"));

        String result = client.search("test query");

        assertNotNull(result);
    }

    @Test
    void shouldReturnErrorMessageAfterMaxRetries() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        String result = client.search("test query");

        assertTrue(result.contains("搜索服务暂时不可用"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=SearXNGClientTest`
Expected: FAIL - class not found

- [ ] **Step 3: Add OkHttp dependency to pom.xml**

在 `<dependencies>` 中添加：

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
</dependency>
```

在 `<properties>` 中添加：

```xml
<okhttp.version>4.12.0</okhttp.version>
```

在测试依赖中添加：

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 4: Write minimal implementation**

```java
package com.tripweaver.client;

import com.tripweaver.config.SearXNGProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
public class SearXNGClient {

    private final SearXNGProperties properties;
    private final OkHttpClient httpClient;

    public SearXNGClient(SearXNGProperties properties) {
        this.properties = properties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String search(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = properties.getBaseUrl() + "/search?q=" + encodedQuery + "&format=json";

        for (int attempt = 1; attempt <= properties.getRetryCount(); attempt++) {
            try {
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body().string();
                    }
                    log.warn("SearXNG request failed with status: {}", response.code());
                }
            } catch (Exception e) {
                log.warn("SearXNG request attempt {} failed: {}", attempt, e.getMessage());
            }

            if (attempt < properties.getRetryCount()) {
                try {
                    Thread.sleep(properties.getRetryDelayMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return "搜索服务暂时不可用，请稍后重试。";
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=SearXNGClientTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add backend/pom.xml backend/src/main/java/com/tripweaver/client/SearXNGClient.java backend/src/test/java/com/tripweaver/client/SearXNGClientTest.java
git commit -m "feat: add SearXNG HTTP client with retry logic"
```

---

### Task 3: Web Search 工具

**Files:**
- Create: `backend/src/main/java/com/tripweaver/tools/WebSearchTool.java`
- Test: `backend/src/test/java/com/tripweaver/tools/WebSearchToolTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.tripweaver.tools;

import com.tripweaver.client.SearXNGClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSearchToolTest {

    @Mock
    private SearXNGClient searxngClient;

    @InjectMocks
    private WebSearchTool webSearchTool;

    @Test
    void shouldCallClientWithQuery() {
        when(searxngClient.search("北京到上海高铁")).thenReturn("{\"results\": []}");

        String result = webSearchTool.search("北京到上海高铁");

        assertNotNull(result);
        verify(searxngClient).search("北京到上海高铁");
    }

    @Test
    void shouldReturnErrorMessageWhenClientFails() {
        when(searxngClient.search(anyString())).thenReturn("搜索服务暂时不可用，请稍后重试。");

        String result = webSearchTool.search("test");

        assertTrue(result.contains("搜索服务暂时不可用"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=WebSearchToolTest`
Expected: FAIL - class not found

- [ ] **Step 3: Write minimal implementation**

```java
package com.tripweaver.tools;

import com.tripweaver.client.SearXNGClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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

- [ ] **Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=WebSearchToolTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/tripweaver/tools/WebSearchTool.java backend/src/test/java/com/tripweaver/tools/WebSearchToolTest.java
git commit -m "feat: add WebSearchTool with @Tool annotation"
```

---

### Task 4: 删除旧工具文件

**Files:**
- Delete: `backend/src/main/java/com/tripweaver/tools/WeatherTool.java`
- Delete: `backend/src/main/java/com/tripweaver/tools/ToolConfig.java`
- Delete: `backend/src/test/java/com/tripweaver/ai/AiServiceTest.java` (旧测试)

- [ ] **Step 1: Delete WeatherTool.java**

```bash
git rm backend/src/main/java/com/tripweaver/tools/WeatherTool.java
```

- [ ] **Step 2: Delete ToolConfig.java**

```bash
git rm backend/src/main/java/com/tripweaver/tools/ToolConfig.java
```

- [ ] **Step 3: Delete old AiServiceTest.java**

```bash
git rm backend/src/test/java/com/tripweaver/ai/AiServiceTest.java
```

- [ ] **Step 4: Commit**

```bash
git commit -m "refactor: remove WeatherTool and ToolConfig"
```

---

### Task 5: 改造 AiService

**Files:**
- Modify: `backend/src/main/java/com/tripweaver/ai/AiService.java`
- Create: `backend/src/test/java/com/tripweaver/ai/AiServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.tripweaver.ai;

import com.tripweaver.tools.WebSearchTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private WebSearchTool webSearchTool;

    @Test
    void shouldIncludeWebSearchTool() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.tools(any(WebSearchTool.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("测试响应");

        AiService aiService = new AiService(chatClient, webSearchTool);
        String result = aiService.chat("你好");

        assertEquals("测试响应", result);
        verify(requestSpec).tools(webSearchTool);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=AiServiceTest`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
package com.tripweaver.ai;

import com.tripweaver.tools.WebSearchTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final WebSearchTool webSearchTool;

    private static final String BASE_SYSTEM_PROMPT = """
        你是一个专业的旅行规划助手。你的任务是帮助用户规划完美的旅行。

        当用户提供旅行需求时，你需要：
        1. 理解用户的目的地、时间、预算、偏好等信息
        2. 如果信息不完整，礼貌地询问缺失的关键信息
        3. 当信息足够时，生成详细的旅行计划

        旅行计划必须包含以下要点：

        ## 核心要点（必须包含）

        ### 1. 行程安排
        - 每日行程时间线
        - 景点/活动安排
        - 游玩时长建议

        ### 2. 住宿推荐
        - 推荐酒店/民宿
        - 预订建议
        - 价格区间

        ### 3. 交通方案
        - 往返交通方式
        - 当地交通建议
        - 交通费用估算

        ### 4. 美食推荐
        - 特色美食介绍
        - 餐厅推荐
        - 人均消费

        ### 5. 预算估算
        - 各项费用明细
        - 总预算建议
        - 省钱小贴士

        ### 6. 天气信息
        - 目的地天气情况
        - 穿衣建议
        - 注意事项

        ## 可选要点（根据目的地情况提供）

        ### 7. 证件准备
        - 身份证/护照要求
        - 签证信息（如需）
        - 其他证件

        ### 8. 安全提示
        - 目的地安全状况
        - 注意事项
        - 紧急联系方式

        ### 9. 健康建议
        - 医疗准备
        - 常备药品
        - 特殊健康提醒

        ### 10. 行李清单
        - 必备物品
        - 推荐携带
        - 禁止携带

        ### 11. 通讯建议
        - 电话卡/网络
        - 当地通讯方式
        """;

    public String chat(String userMessage) {
        return chatClient.prompt()
                .system(BASE_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(webSearchTool)
                .call()
                .content();
    }

    public String chat(String userMessage, String conversationHistory) {
        String fullPrompt = conversationHistory + "\n\n用户: " + userMessage;
        return chatClient.prompt()
                .system(BASE_SYSTEM_PROMPT)
                .user(fullPrompt)
                .tools(webSearchTool)
                .call()
                .content();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=AiServiceTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/tripweaver/ai/AiService.java backend/src/test/java/com/tripweaver/ai/AiServiceTest.java
git commit -m "feat: integrate WebSearchTool into AiService"
```

---

### Task 6: 配置 application.yml

**Files:**
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: Add searxng configuration**

在 `application.yml` 末尾添加：

```yaml
searxng:
  base-url: ${SEARXNG_BASE_URL}
  retry-count: 3
  retry-delay-ms: 1000
```

- [ ] **Step 2: Enable configuration properties scanning**

在 `TripWeaverApplication.java` 中添加 `@EnableConfigurationProperties`：

```java
package com.tripweaver;

import com.tripweaver.config.SearXNGProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SearXNGProperties.class)
public class TripWeaverApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripWeaverApplication.class, args);
    }
}
```

- [ ] **Step 3: Run all tests to verify**

Run: `cd backend && mvn test`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/resources/application.yml backend/src/main/java/com/tripweaver/TripWeaverApplication.java
git commit -m "feat: add searxng configuration to application.yml"
```

---

### Task 7: 更新 .env.example

**Files:**
- Modify: `.env.example`

- [ ] **Step 1: Add SEARXNG_BASE_URL**

修改 `.env.example`：

```env
# AI Model Configuration
OPENAI_API_KEY=your-api-key-here
SPRING_AI_OPENAI_BASE_URL=https://api.openai.com
SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=gpt-4o

# JWT Configuration
JWT_SECRET=your-jwt-secret-must-be-at-least-32-characters-long

# SearXNG Configuration
SEARXNG_BASE_URL=http://172.27.16.134:7000
```

- [ ] **Step 2: Commit**

```bash
git add .env.example
git commit -m "docs: add SEARXNG_BASE_URL to .env.example"
```

---

### Task 8: 更新 README.md

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Update configuration table**

在配置说明表格中添加 SearXNG 配置：

```markdown
## 配置说明

| 变量 | 说明 | 必需 |
|------|------|------|
| `OPENAI_API_KEY` | AI 模型 API Key | 是 |
| `JWT_SECRET` | JWT 签名密钥 | 是 |
| `SEARXNG_BASE_URL` | SearXNG 搜索服务地址 | 是 |
| `SPRING_AI_OPENAI_BASE_URL` | API 基础 URL | 否 |
| `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL` | 模型名称 | 否 |
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add SEARXNG_BASE_URL to README configuration"
```

---

### Task 9: 检查 CLAUDE.md

**Files:**
- Check: `CLAUDE.md`

- [ ] **Step 1: Review CLAUDE.md for needed updates**

检查 `CLAUDE.md`，确认是否需要更新：
- 环境配置部分是否需要添加 SearXNG 说明
- 技术栈是否需要更新

当前 `CLAUDE.md` 的环境配置部分：

```markdown
### 环境配置

复制 `.env.example` 为 `.env` 并填入配置：

```bash
cp .env.example .env
```

必需配置：
- `OPENAI_API_KEY` - AI 模型 API Key
- `JWT_SECRET` - JWT 签名密钥（至少32字符）
```

- [ ] **Step 2: Update CLAUDE.md if needed**

如果需要更新，添加 SearXNG 配置说明：

```markdown
必需配置：
- `OPENAI_API_KEY` - AI 模型 API Key
- `JWT_SECRET` - JWT 签名密钥（至少32字符）
- `SEARXNG_BASE_URL` - SearXNG 搜索服务地址
```

- [ ] **Step 3: Commit if changed**

```bash
git add CLAUDE.md
git commit -m "docs: add SEARXNG_BASE_URL to CLAUDE.md"
```

---

### Task 10: 运行完整测试

- [ ] **Step 1: Run all backend tests**

Run: `cd backend && mvn test`
Expected: All tests PASS

- [ ] **Step 2: Verify application starts**

Run: `cd backend && mvn spring-boot:run`
Expected: Application starts without errors

- [ ] **Step 3: Final commit if any fixes needed**

```bash
git add .
git commit -m "fix: resolve test failures"
```
