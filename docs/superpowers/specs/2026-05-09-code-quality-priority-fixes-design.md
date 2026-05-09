# 代码质量优先修复 — 设计方案

> 日期: 2026-05-09
> 基于: docs/code-analysis-report.md

## 概述

从代码分析报告中选出对后续开发影响最大、性价比最高的 6 项修复，优先理顺代码质量和架构基础。

## 优先级排序

| 顺序 | 事项 | 影响 | 依赖 |
|------|------|------|------|
| 1 | SQLite → HSQLDB 数据库切换 | ChatMemory 持久化的前提 | 无 |
| 2 | JSON 注入修复 → JDBC ChatMemory 迁移 | 消除核心数据损坏风险 | 需要 #1 |
| 3 | TravelPlan 添加 status 字段 | 前后端数据一致性 | 无 |
| 4 | 自定义异常 + 全局异常处理器 | 统一错误响应 | 无 |
| 5 | 前端 axios 实例统一 | 消除重复代码 | 无 |
| 6 | createNewPlan 添加 @Transactional | 数据一致性保障 | 无 |

---

## 1. SQLite → HSQLDB 数据库切换

### 背景

SQLite 使用社区 Hibernate 方言，且不在 Spring AI JDBC ChatMemory 支持列表中。切换到 HSQLDB（纯 Java 嵌入式数据库）可获得官方 Hibernate 方言和 Spring AI JDBC ChatMemory 官方支持。

### 改动

| 文件 | 改动 |
|------|------|
| `backend/pom.xml` | 替换 `sqlite-jdbc` → `hsqldb`，添加 `spring-ai-starter-model-chat-memory-repository-jdbc` |
| `backend/src/main/resources/application.yml` | 修改 datasource url/driver/dialect |
| `backend/src/test/resources/application-test.yml` | 测试环境切换 HSQLDB 或保持 H2 |

JDBC URL: `jdbc:hsqldb:file:./data/tripweaver;shutdown=true`
Driver: `org.hsqldb.jdbc.JDBCDriver`
Dialect: `org.hibernate.dialect.HSQLDialect`

### 注意事项

- HSQLDB 文件模式会生成多个文件（`.data`, `.log`, `.properties` 等），需更新 `.gitignore`
- DDL: 保持 `update`，Hibernate 会自动建表

---

## 2. JSON 注入修复 → JDBC ChatMemory 迁移

### 背景

`ChatService.updateConversationHistory()` 使用字符串 replace 拼接 JSON，存在数据损坏风险。使用 Spring AI 的 `MessageChatMemoryAdvisor` + `JdbcChatMemoryRepository` 替代。

### 改动

| 文件 | 改动 |
|------|------|
| `backend/src/main/resources/application.yml` | 添加 `spring.ai.chat.memory.repository.jdbc.initialize-schema=always` |
| `backend/src/main/java/com/tripweaver/config/AiConfig.java` | 注入 `JdbcChatMemoryRepository`，构建 `MessageWindowChatMemory` + `MessageChatMemoryAdvisor`，通过 `defaultAdvisors()` 绑定到 ChatClient |
| `backend/src/main/java/com/tripweaver/ai/AiService.java` | `chat()` 方法改为单参数 `chat(String userMessage)`，删除手动拼接历史参数的版本 |
| `backend/src/main/java/com/tripweaver/service/ChatService.java` | `sendMessage()` 中通过 `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, planId.toString()))` 传递会话 ID；删除 `updateConversationHistory()` 和 `escapeJson()` 方法 |
| `backend/src/main/java/com/tripweaver/entity/Conversation.java` | `messages` 字段暂时保留，后续可用于历史归档 |

### 数据流

```
之前: ChatService 读 messages JSON → 拼历史字符串 → AiService 拼到 user prompt
之后: ChatService 传 conversationId → MessageChatMemoryAdvisor 自动加载/追加消息到 ChatMemory → JDBC 持久化
```

---

## 3. TravelPlan 添加 status 字段

### 背景

前端 `PlansView.vue:43` 和 `PlanDetailView.vue` 使用了 `plan.status`，但后端实体没有此字段。

### 改动

| 文件 | 改动 |
|------|------|
| `backend/src/main/java/com/tripweaver/entity/TravelPlan.java` | 添加 `private String status = "draft";` |

### 状态值

| 值 | 含义 |
|----|------|
| `draft` | 初始创建 |
| `planning` | AI 已生成内容 |
| `confirmed` | 用户确认 |
| `completed` | 归档 |

---

## 4. 自定义异常 + 全局异常处理器

### 背景

`UserService` 直接用 `RuntimeException` 表示业务错误，前端无法区分错误类型，全部返回 500。

### 改动

| 文件 | 改动 |
|------|------|
| `backend/src/main/java/com/tripweaver/exception/BusinessException.java` | 新增：业务异常基类，含 `code` 字段 |
| `backend/src/main/java/com/tripweaver/exception/UserAlreadyExistsException.java` | 新增：用户名/邮箱冲突，HTTP 409 |
| `backend/src/main/java/com/tripweaver/exception/AuthenticationException.java` | 新增：登录/认证失败，HTTP 401 |
| `backend/src/main/java/com/tripweaver/config/GlobalExceptionHandler.java` | 新增：`@RestControllerAdvice`，处理 `BusinessException` 和其他异常 |
| `backend/src/main/java/com/tripweaver/service/UserService.java` | 替换 `RuntimeException` → 具体业务异常 |

### 异常层次

```
BusinessException (base, code field)
├── UserAlreadyExistsException (409)
└── AuthenticationException (401)
```

---

## 5. 前端 axios 实例统一

### 背景

`auth.js` 和 `chat.js` 各自创建相同的 axios 实例和拦截器。

### 改动

| 文件 | 改动 |
|------|------|
| `frontend/src/api/client.js` | 新增：统一导出配置好的 axios 实例（含 token 拦截器） |
| `frontend/src/api/auth.js` | 改为从 `client.js` 导入 axios 实例 |
| `frontend/src/api/chat.js` | 改为从 `client.js` 导入 axios 实例 |

---

## 6. createNewPlan 添加 @Transactional

### 背景

`ChatService.createNewPlan()` 依次保存 TravelPlan 和 Conversation，无事务保护。

### 改动

| 文件 | 改动 |
|------|------|
| `backend/src/main/java/com/tripweaver/service/ChatService.java` | 在 `createNewPlan()` 方法上加 `@Transactional` |
