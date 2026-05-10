# TripWeaver 项目改进分析

## 项目概况

| 方面 | 详情 |
|------|------|
| **后端** | Java 21, Spring Boot 3.5, Spring AI 1.1.6 |
| **前端** | Vue 3, Vite 8, Pinia, Vue Router, Axios |
| **数据库** | HSQLDB (文件模式), 测试用 H2 内存库 |
| **认证** | JWT (jjwt 0.12.6), Spring Security |
| **搜索** | SearXNG 自托管服务, OkHttp 客户端 |
| **测试** | JUnit 5 + Mockito (后端), Vitest + Playwright (前端) |

## 已实现功能

1. **用户认证系统** — 注册/登录/获取当前用户, JWT 令牌验证, 路由守卫
2. **AI 旅行规划聊天** — Spring AI ChatClient 对话式规划, 对话记忆 (JDBC ChatMemory), Markdown 渲染响应
3. **网络搜索集成** — SearXNG 客户端含重试逻辑, 作为 Spring AI @Tool 暴露给模型
4. **旅行计划 CRUD** — 创建/列表/详情/更新/删除, 上下文菜单操作
5. **前端 UI/UX** — Explorer Map 主题, 响应式设计, 打字指示器, 空状态插图

---

## 关键问题 (Bug)

### 1. `GET /api/auth/me` 泄露密码哈希

**位置**: `backend/src/main/java/com/tripweaver/controller/AuthController.java:33`

`getCurrentUser()` 直接返回完整的 `User` 实体，包含 `password` 字段（BCrypt 哈希）。前端接收后虽然不使用，但已明文传输到客户端。

**修复方向**: 返回 DTO，排除 `password` 字段；或使用 `@JsonIgnore` 注解。

### 2. TravelPlan 实体字段缺失

**位置**: `backend/src/main/java/com/tripweaver/entity/TravelPlan.java`

前端在 `PlanDetailView.vue` 和 `PlansView.vue` 中使用了以下字段，但实体中不存在：

| 字段 | 类型 | 用途 |
|------|------|------|
| `status` | String | 草稿 / 规划中 / 已确认 / 已完成 |
| `itinerary` | JSON Array | 每日行程详情 |
| `notes` | String (Markdown) | 用户备注 |
| `travelers` | Integer | 出行人数 |

**影响**: 更新这些字段时实际不会持久化到数据库。

### 3. 消息未持久化

**位置**: `backend/src/main/java/com/tripweaver/service/ChatService.java`

`sendMessage()` 方法将消息存储完全委托给 Spring AI 的 JDBC ChatMemory 表，但没有将消息写入应用层的 `conversations` 表。这意味着应用层无法查询历史消息记录。

### 4. 无全局异常处理

**位置**: 项目中无 `@ControllerAdvice` 类

`UserService` 和 `PlanService` 中抛出的 `RuntimeException`（如 "用户已存在"、"计划未找到"）会直接返回 HTTP 500 及堆栈信息，而非有意义的业务错误响应。

---

## 工程化改进

### 5. API 文档缺失

**现状**: 无 Swagger / OpenAPI / SpringDoc 集成。
**建议**: 引入 `springdoc-openapi-starter-webmvc-ui`，自动生成交互式 API 文档。

### 6. CORS 配置缺失

**现状**: 仅依赖 Vite 开发代理 (`/api` → `localhost:8080`)。生产环境下前后端分离部署时跨域请求会失败。
**建议**: 在 `SecurityConfig` 中添加 `CorsConfigurationSource` Bean。

### 7. JWT 过期无处理

**现状**: 
- Token 固定 24 小时过期
- 无刷新令牌机制
- 前端 axios 拦截器未处理 401 响应（无自动跳转登录页）

**建议**: 在前端 axios 拦截器中添加 401 响应处理，如清除 token 并重定向到 `/login`。

### 8. 无分页

**位置**: `GET /api/plans` — `PlanController.java`

直接返回 `planRepository.findAllByUserId()` 的全部结果。

**建议**: 使用 Spring Data 的 `Pageable` 参数，按页返回。

### 9. 无速率限制

**现状**: 聊天端点没有任何节流措施。
**建议**: 引入 Bucket4j 或 Spring Cloud Gateway 实现 IP/用户级别的速率限制。

### 10. 密码验证较弱

**现状**: 
- 仅要求 `@Size(min = 6)`，无复杂度要求
- 注册时无密码确认字段（二次输入确认）

### 11. application.yml 不安全默认值

**位置**: `backend/src/main/resources/application.yml`

```yaml
spring.ai.openai.api-key: sk-test-key
jwt.secret: test-secret-key-must-be-at-least-32-characters-long
```

如果 `.env` 文件未正确配置，这些默认值可能在生产中被误用。

**建议**: 移除默认值，或改为空字符串 + `@ConditionalOnProperty` 检验。

### 12. 无登出端点

**现状**: 前端登出仅清除 `localStorage` 中的 token，服务端无任何处理。
**建议**: 可添加 token 黑名单机制（Redis），或简单保持无状态设计。

### 13. 计划删除无级联

删除计划不会清理 `conversations` 表或 Spring AI 聊天记忆表中的关联数据，造成孤立记录。

### 14. PlanRepository 删除操作静默失败

`deleteByIdAndUserId()` 在记录不存在时不会抛出异常，调用方无法区分「删除成功」和「记录不存在」。

---

## 测试覆盖

### 后端测试

| 已有测试 | 缺失测试 |
|----------|----------|
| AuthControllerTest ✅ | **ChatServiceTest** ❌ |
| PlanControllerTest (基础) | **ChatControllerTest** ❌ |
| UserServiceTest ✅ | **PlanServiceTest** ❌ |
| AiServiceTest ✅ | PlanControllerTest (扩展) |
| JwtTokenProviderTest ✅ | |
| SearXNGClientTest ✅ | |
| WebSearchToolTest ✅ | |
| Repository 测试 ✅ | |

### 前端测试

| 已有测试 | 缺失测试 |
|----------|----------|
| userStore.test.js (3 个) | **HomeView** 测试 ❌ |
| auth.spec.ts (3 个 E2E) | **LoginView** 测试 ❌ |
| chat.spec.ts (1 个 E2E) | **RegisterView** 测试 ❌ |
| | **ChatView** 测试 ❌ |
| | **PlansView** 测试 ❌ |
| | **PlanDetailView** 测试 ❌ |

---

## 建议优先级

| 优先级 | 项目 | 类型 | 学习价值 |
|--------|------|------|----------|
| 🔴 P0 | 密码哈希泄露 | Bug | DTO 设计 / Jackson 序列化 |
| 🔴 P0 | TravelPlan 字段缺失 | Bug | ORM 实体映射 |
| 🔴 P0 | 全局异常处理 | 工程化 | Spring `@ControllerAdvice` |
| 🟡 P1 | API 文档 (Swagger) | 工程化 | OpenAPI 规范 |
| 🟡 P1 | CORS 配置 | 工程化 | 跨域安全 |
| 🟡 P1 | JWT 过期前端处理 | 体验 | axios 拦截器 |
| 🟡 P1 | 分页 | 工程化 | Spring Data Pageable |
| 🟢 P2 | 补充后端测试 | 测试 | Mockito 单元测试 |
| 🟢 P2 | 消息持久化 | Bug | JPA 持久化 |
| 🟢 P2 | 密码复杂度 / 确认 | 体验 | Bean Validation |
| ⚪ P3 | 速率限制 | 安全 | Bucket4j 限流 |
| ⚪ P3 | 登出 / Token 黑名单 | 安全 | Redis / 内存黑名单 |
| ⚪ P3 | 级联删除 | 数据一致性 | JPA cascade |
