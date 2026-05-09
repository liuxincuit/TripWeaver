# TripWeaver 安全修复与 AI 服务重构设计

> 日期: 2026-05-09

## 概述

本设计解决代码分析报告中的 6 个高优先级安全问题，并重构 AI 服务层以使用 Spring AI ChatMemory。

## 一、安全修复

### 1. XSS 漏洞修复

**问题**：前端使用 `v-html` 直接渲染 Markdown，可能导致 XSS 攻击。

**影响文件**：
- `frontend/src/views/ChatView.vue`
- `frontend/src/views/PlanDetailView.vue`

**解决方案**：
1. 安装 `dompurify` 依赖
2. 创建 `frontend/src/utils/markdown.js` 统一处理 Markdown 渲染
3. 将 `marked.parse()` 输出通过 `DOMPurify.sanitize()` 消毒

```javascript
// frontend/src/utils/markdown.js
import DOMPurify from 'dompurify'
import { marked } from 'marked'

export function sanitizeMarkdown(content) {
  return DOMPurify.sanitize(marked.parse(content || ''))
}
```

---

### 2. JWT 密钥移除默认值

**问题**：生产环境配置文件中使用默认的测试密钥。

**影响文件**：
- `backend/src/main/resources/application.yml`
- `backend/src/main/java/com/tripweaver/config/SecurityConfig.java`（新增验证）

**解决方案**：
1. 移除 `jwt.secret` 的默认值
2. 添加启动时验证，确保 JWT 密钥已配置且长度至少 32 字符

```yaml
jwt:
  secret: ${JWT_SECRET}  # 移除默认值，强制环境变量
```

---

### 3. 全局异常处理器

**问题**：没有 `@ControllerAdvice`，服务层异常直接返回 500 错误。

**新增文件**：
- `backend/src/main/java/com/tripweaver/exception/ErrorResponse.java`
- `backend/src/main/java/com/tripweaver/exception/GlobalExceptionHandler.java`

**解决方案**：
创建 `@RestControllerAdvice` 处理全局异常，定义统一错误响应格式。

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("Access denied"));
    }
}
```

---

### 4. CORS 配置

**问题**：后端没有显式配置 CORS，生产部署会出现跨域问题。

**影响文件**：
- `backend/src/main/java/com/tripweaver/config/SecurityConfig.java`

**解决方案**：
添加 `CorsConfigurationSource` Bean，配置允许的来源和方法。

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(
        "http://localhost:5173",  // 开发环境
        "${CORS_ALLOWED_ORIGINS:}"  // 生产环境从环境变量读取
    ));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### 5. 前端 axios 实例统一

**问题**：`auth.js` 和 `chat.js` 分别创建了独立的 axios 实例。

**影响文件**：
- `frontend/src/api/auth.js`
- `frontend/src/api/chat.js`

**新增文件**：
- `frontend/src/api/client.js`

**解决方案**：
创建统一的 axios 实例，其他模块导入使用。

```javascript
// frontend/src/api/client.js
import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default client
```

---

### 6. JSON 注入修复 + ChatMemory 迁移

**问题**：`ChatService.updateConversationHistory()` 使用字符串拼接更新 JSON。

**解决方案**：迁移到 Spring AI ChatMemory，彻底解决此问题。

详见下一节。

---

## 二、AI 服务重构

### ChatMemory 架构

**新增依赖**：
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
</dependency>
```

**影响文件**：
- `backend/pom.xml` - 添加依赖
- `backend/src/main/java/com/tripweaver/config/AiConfig.java` - 配置 ChatMemory
- `backend/src/main/java/com/tripweaver/ai/AiService.java` - 使用 MessageChatMemoryAdvisor
- `backend/src/main/java/com/tripweaver/service/ChatService.java` - 简化，移除手动 JSON 拼接

### 配置 ChatMemory

```java
// AiConfig.java
@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(repository)
            .maxMessages(20)
            .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }
}
```

### AiService 重构

```java
// AiService.java
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final WebSearchTool webSearchTool;
    private final ChatMemory chatMemory;

    private static final String SYSTEM_PROMPT = """
        你是一个专业的旅行规划助手...
        """;

    public String chat(String userMessage, String conversationId) {
        return chatClient.prompt()
            .system(SYSTEM_PROMPT)
            .user(userMessage)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .tools(webSearchTool)
            .call()
            .content();
    }
}
```

### ChatService 简化

```java
// ChatService.java
public String sendMessage(Long planId, String message) {
    User user = userService.getCurrentUser();

    // 获取或创建对话（仅用于关联 planId）
    Conversation conversation = conversationRepository
        .findByPlanIdAndUserId(planId, user.getId())
        .orElseGet(() -> createConversation(user.getId(), planId));

    // 使用 conversationId 作为 ChatMemory 的会话标识
    String conversationId = conversation.getId().toString();

    // 调用 AI 服务，ChatMemory 自动管理历史
    return aiService.chat(message, conversationId);
}
```

---

## 三、数据库变更

### 新增表（自动创建）

Spring AI ChatMemory 会自动创建 `chat_memory` 表：

```sql
CREATE TABLE chat_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 现有表处理

`conversations` 表保留，用于：
- 关联 `planId` 和 `userId`
- 存储会话元数据

`messages` 字段不再使用，可在后续版本中移除。

---

## 四、测试计划

1. **单元测试**：GlobalExceptionHandler 异常处理
2. **集成测试**：ChatMemory 持久化验证
3. **端到端测试**：
   - 注册新用户
   - 登录验证
   - 发送消息并验证 AI 回复
   - 验证对话历史持久化

---

## 五、实施顺序

1. 前端：安装 dompurify，创建 markdown 工具函数
2. 前端：统一 axios 实例
3. 后端：添加 ChatMemory 依赖
4. 后端：配置 ChatMemory 和 AiConfig
5. 后端：重构 AiService 和 ChatService
6. 后端：添加全局异常处理器
7. 后端：添加 CORS 配置
8. 后端：移除 JWT 默认值，添加启动验证
9. 端到端测试验证
