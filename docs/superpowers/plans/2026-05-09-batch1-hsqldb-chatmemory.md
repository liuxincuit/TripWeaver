# Batch 1: HSQLDB + ChatMemory Migration

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将数据库从 SQLite 切换到 HSQLDB，用 Spring AI JDBC ChatMemory + MessageChatMemoryAdvisor 替代手拼 JSON 字符串的对话历史管理。

**Architecture:** 用 Spring AI 内置的 `MessageChatMemoryAdvisor` 自动管理 AI 对话上下文，`JdbcChatMemoryRepository` 将对话记忆持久化到 HSQLDB 的 `SPRING_AI_CHAT_MEMORY` 表。ChatService 不再手动拼接 JSON 历史字符串，改为传 conversationId。

**Tech Stack:** Spring Boot 3.5.14, Spring AI 1.1.6, HSQLDB, Hibernate

---

## File Structure

| 文件 | 职责 | 改动类型 |
|------|------|----------|
| `backend/pom.xml` | 依赖管理 | 修改：替换 sqlite → hsqldb，加 ChatMemory JDBC |
| `backend/src/main/resources/application.yml` | 生产配置 | 修改：HSQLDB 数据源，ChatMemory JDBC |
| `backend/src/main/java/com/tripweaver/config/AiConfig.java` | ChatClient 配置 | 修改：注入 ChatMemory，绑定 MessageChatMemoryAdvisor |
| `backend/src/main/java/com/tripweaver/ai/AiService.java` | AI 调用 | 修改：chat() 签名改为接收 conversationId |
| `backend/src/main/java/com/tripweaver/service/ChatService.java` | 对话业务 | 修改：移除手拼 JSON，传 conversationId |
| `backend/src/test/java/com/tripweaver/config/TestAiConfig.java` | 测试 ChatMemory | 新增：InMemory ChatMemory Bean（因为 H2 不支持 JDBC ChatMemory） |

注意：测试配置 (`application-test.yml`) 保持 H2 不变，已有的 15 个测试不受影响。

---

### Task 1: 替换 SQLite → HSQLDB，添加 ChatMemory JDBC 依赖

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: 修改 pom.xml — 替换数据库依赖**

将以下两行：

```xml
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <scope>runtime</scope>
        </dependency>
```

和：

```xml
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
        </dependency>
```

替换为 HSQLDB 依赖（版本由 Spring Boot Parent POM 管理，不写 version）：

```xml
        <dependency>
            <groupId>org.hsqldb</groupId>
            <artifactId>hsqldb</artifactId>
            <scope>runtime</scope>
        </dependency>
```

同时添加 JDBC ChatMemory 依赖（版本由 Spring AI BOM 管理）：

```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
        </dependency>
```

- [ ] **Step 2: 修改 application.yml — HSQLDB + ChatMemory 配置**

将 `backend/src/main/resources/application.yml` 中的 datasource、jpa dialect、searxng 块修改如下：

```yaml
  datasource:
    url: jdbc:hsqldb:file:./data/tripweaver;shutdown=true
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.HSQLDialect
```

删除 spring.ai 配置块末尾的 `temperature` 行后的空行，确保结构完整。

在 `spring:` 层级下添加 ChatMemory JDBC 配置（与 `datasource:` 同级）：

```yaml
  ai:
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always
```

注意：上述配置需要合并到现有的 `spring.ai.openai.*` 配置中，不要创建重复的 `spring.ai` 键。

- [ ] **Step 3: 删除旧 SQLite 数据库文件**

运行：

```bash
rm -f D:/code/TripWeaver/backend/data/tripweaver.db
```

（HSQLDB 会在首次启动时自动创建新的数据库文件）

- [ ] **Step 4: 运行测试验证依赖正确**

```bash
cd D:/code/TripWeaver/backend && mvn test -q
```

预期：BUILD SUCCESS，15 个测试全部通过。

注意：`application-test.yml` 保持使用 H2 内存数据库，不受 HSQLDB 影响。

- [ ] **Step 5: 提交**

```bash
git add backend/pom.xml backend/src/main/resources/application.yml
git commit -m "build: switch database from SQLite to HSQLDB, add ChatMemory JDBC dependency

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 2: 新建 TestAiConfig — 为测试环境提供 InMemory ChatMemory

**Files:**
- Create: `backend/src/test/java/com/tripweaver/config/TestAiConfig.java`

**Why:** AiConfig（Task 3）需要 `ChatMemory` Bean。测试环境使用 H2，不在 JDBC ChatMemory 支持的方言列表中，先建好测试 Bean 再改 AiConfig。

- [ ] **Step 1: 创建 TestAiConfig.java**

新建 `backend/src/test/java/com/tripweaver/config/TestAiConfig.java`：

```java
package com.tripweaver.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestAiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }
}
```

- [ ] **Step 2: 运行测试验证**

```bash
cd D:/code/TripWeaver/backend && mvn test -q
```

预期：BUILD SUCCESS，15 个测试全部通过。

- [ ] **Step 3: 提交**

```bash
git add backend/src/test/java/com/tripweaver/config/TestAiConfig.java
git commit -m "test: add TestAiConfig with InMemory ChatMemory for H2 test env

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 3: 配置 MessageChatMemoryAdvisor

**Files:**
- Modify: `backend/src/main/java/com/tripweaver/config/AiConfig.java`

- [ ] **Step 1: 修改 AiConfig.java — 注入 ChatMemory 并绑定 Advisor**

将 `backend/src/main/java/com/tripweaver/config/AiConfig.java` 的内容替换为：

```java
package com.tripweaver.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
```

`ChatMemory` 由 Spring AI 自动配置（生产环境使用 `JdbcChatMemoryRepository`）或 `TestAiConfig`（测试环境使用 `InMemoryChatMemoryRepository`），无需手动定义 Bean。

- [ ] **Step 2: 运行测试验证**

```bash
cd D:/code/TripWeaver/backend && mvn test -q
```

预期：BUILD SUCCESS，AiConfig 能正确接收到 `TestAiConfig` 提供的 `ChatMemory` Bean。

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/tripweaver/config/AiConfig.java
git commit -m "feat: wire MessageChatMemoryAdvisor with ChatMemory

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 4: 修改 AiService — chat() 签名改为接收 conversationId

**Files:**
- Modify: `backend/src/main/java/com/tripweaver/ai/AiService.java`
- Test: `backend/src/test/java/com/tripweaver/ai/AiServiceTest.java`

- [ ] **Step 1: 修改 AiService.java**

将 `chat(String userMessage, String conversationHistory)` 方法替换为接收 conversationId：

```java
package com.tripweaver.ai;

import com.tripweaver.tools.WebSearchTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final WebSearchTool webSearchTool;

    private static final String SYSTEM_PROMPT = """
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
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .tools(webSearchTool)
                .call()
                .content();
    }

    public String chat(String userMessage, String conversationId) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .tools(webSearchTool)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
```

- [ ] **Step 2: 运行 AiServiceTest 验证**

```bash
cd D:/code/TripWeaver/backend && mvn test -Dtest=AiServiceTest -q
```

预期：1 个测试 `shouldIncludeWebSearchTool` 通过。该测试只调用 `chat("你好")` 单参方法，不涉及 conversationId。

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/tripweaver/ai/AiService.java
git commit -m "feat: add conversationId parameter to AiService.chat() for ChatMemory

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 5: 清理 ChatService — 移除手拼 JSON，使用 conversationId

**Files:**
- Modify: `backend/src/main/java/com/tripweaver/service/ChatService.java`

- [ ] **Step 1: 修改 ChatService.java**

将文件内容替换为：

```java
package com.tripweaver.service;

import com.tripweaver.ai.AiService;
import com.tripweaver.entity.Conversation;
import com.tripweaver.entity.TravelPlan;
import com.tripweaver.entity.User;
import com.tripweaver.repository.ConversationRepository;
import com.tripweaver.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiService aiService;
    private final ConversationRepository conversationRepository;
    private final PlanRepository planRepository;
    private final UserService userService;

    public String sendMessage(Long planId, String message) {
        User user = userService.getCurrentUser();

        // 确保对话记录存在
        Conversation conversation = conversationRepository.findByPlanIdAndUserId(planId, user.getId())
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setUserId(user.getId());
                    newConv.setPlanId(planId);
                    newConv.setMessages("[]");
                    return conversationRepository.save(newConv);
                });

        // ChatMemory Advisor 基于 conversationId 自动管理对话历史
        String response = aiService.chat(message, planId.toString());

        return response;
    }

    public Long createNewPlan() {
        User user = userService.getCurrentUser();

        TravelPlan plan = new TravelPlan();
        plan.setUserId(user.getId());
        plan.setTitle("新旅行计划");

        TravelPlan savedPlan = planRepository.save(plan);

        // 创建对应的对话
        Conversation conversation = new Conversation();
        conversation.setUserId(user.getId());
        conversation.setPlanId(savedPlan.getId());
        conversation.setMessages("[]");
        conversationRepository.save(conversation);

        return savedPlan.getId();
    }

    public Optional<Conversation> getConversation(Long planId) {
        User user = userService.getCurrentUser();
        return conversationRepository.findByPlanIdAndUserId(planId, user.getId());
    }
}
```

关键改动：
- 删除了 `private String updateConversationHistory(...)` 方法（原 72-78 行）
- 删除了 `private String escapeJson(...)` 方法（原 81-84 行）
- `sendMessage()` 不再读取 `conversation.messages` 和拼接历史字符串
- `sendMessage()` 改为调用 `aiService.chat(message, planId.toString())`，ChatMemory 自动管理上下文

- [ ] **Step 2: 运行所有测试验证**

```bash
cd D:/code/TripWeaver/backend && mvn test -q
```

预期：BUILD SUCCESS，15 个测试全部通过。

ChatService 本身没有单元测试，但 AuthControllerTest 和 PlanControllerTest 通过 Controller 层间接触及，验证没有编译错误和 Bean 注入失败。

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/tripweaver/service/ChatService.java
git commit -m "refactor: replace manual JSON string concat with ChatMemory advisor

- Remove updateConversationHistory() and escapeJson() methods
- Pass conversationId to AiService for automatic context management
- ChatMemory handles conversation history via MessageChatMemoryAdvisor

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## 验证清单

全部完成后，执行以下最终验证：

- [ ] `mvn test -q` — 15 个测试全部通过
- [ ] 启动后端 `mvn spring-boot:run`，确认 Hibernate 能正常建表（无方言错误）
- [ ] 检查 `backend/data/` 目录生成了 HSQLDB 文件（`.data`, `.log`, `.properties` 等）

## 已知局限

- `GET /api/chat/history/{planId}` 接口在 ChatMemory 接管后，Conversation.messages 不再更新，对新创建的对话将返回空 history。历史检索功能留待后续任务从 ChatMemory 读取并序列化返回。
