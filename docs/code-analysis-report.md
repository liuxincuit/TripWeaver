# TripWeaver 代码库问题分析报告

> 分析日期: 2026-05-09

## 概述

| 优先级 | 问题数量 |
|--------|----------|
| 高 | 6 |
| 中 | 9 |
| 低 | 9 |

---

## 高优先级问题

### 1. XSS 安全漏洞 - 前端 v-html 使用

**问题描述**: 前端使用 `v-html` 直接渲染 Markdown 内容，可能导致 XSS 攻击。

**所在位置**:
- `frontend/src/views/ChatView.vue` 第 100 行
- `frontend/src/views/PlanDetailView.vue` 第 168 行

**建议修复方案**:
```javascript
// 使用 DOMPurify 对 marked 解析结果进行消毒
import DOMPurify from 'dompurify'
function formatMessage(content) {
  return DOMPurify.sanitize(marked.parse(content || ''))
}
```

---

### 2. JWT 密钥配置不安全

**问题描述**: 生产环境配置文件中使用默认的测试密钥，JWT 密钥应该从环境变量获取且不应有默认值。

**所在位置**: `backend/src/main/resources/application.yml` 第 31 行

```yaml
jwt:
  secret: ${JWT_SECRET:test-secret-key-must-be-at-least-32-characters-long}
```

**建议修复方案**:
- 移除默认值，强制要求配置环境变量
- 在启动时验证 JWT 密钥长度和强度

---

### 3. 缺少全局异常处理器

**问题描述**: 没有发现 `@ControllerAdvice` 或 `@ExceptionHandler`，服务层抛出的 `RuntimeException` 会直接返回 500 错误，缺乏友好的错误响应。

**所在位置**: 整个后端项目缺少统一异常处理

**建议修复方案**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(e.getMessage()));
    }
}
```

---

### 4. 缺少 CORS 配置

**问题描述**: 后端没有显式配置 CORS，前端代理仅适用于开发环境，生产部署时会出现跨域问题。

**所在位置**: `backend/src/main/java/com/tripweaver/config/SecurityConfig.java`

**建议修复方案**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("https://your-domain.com"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

---

### 5. 对话历史存储存在 JSON 注入漏洞

**问题描述**: `ChatService.updateConversationHistory()` 方法使用字符串拼接方式更新 JSON，存在 JSON 注入风险，且会导致数据损坏。

**所在位置**: `backend/src/main/java/com/tripweaver/service/ChatService.java` 第 72-78 行

```java
private String updateConversationHistory(String history, String userMessage, String assistantResponse) {
    return history.replace("]", String.format(
        ",{\"role\":\"user\",\"content\":\"%s\"},{\"role\":\"assistant\",\"content\":\"%s\"}]",
        escapeJson(userMessage), escapeJson(assistantResponse)
    ));
}
```

**建议修复方案**:
- 使用 Jackson 或 Gson 库正确解析和序列化 JSON 数组
- 定义消息对象 `List<Message>` 并使用 `ObjectMapper` 序列化

---

### 6. 前端 API 模块重复创建 axios 实例

**问题描述**: `auth.js` 和 `chat.js` 分别创建了独立的 axios 实例，导致代码重复且难以统一管理。

**所在位置**:
- `frontend/src/api/auth.js`
- `frontend/src/api/chat.js`

**建议修复方案**:
创建 `api/client.js` 统一导出配置好的 axios 实例，其他模块导入使用。

---

## 中优先级问题

### 7. 缺少方法级权限控制

**问题描述**: 仅依赖 URL 路径进行权限控制，没有使用 `@PreAuthorize` 等注解进行方法级细粒度权限验证。

**所在位置**: 所有 Controller 类

**建议修复方案**:
```java
@PreAuthorize("authentication.name == #username")
public ResponseEntity<User> getUserProfile(@PathVariable String username) {
    // ...
}
```

---

### 8. Spring AI 版本不一致

**问题描述**: 用户声明使用 Spring AI 1.1.5，但 `pom.xml` 中实际配置的是 1.1.6 版本。

**所在位置**: `backend/pom.xml` 第 22 行

```xml
<spring-ai.version>1.1.6</spring-ai.version>
```

**建议修复方案**: 确认实际需要的版本并统一文档与代码。

---

### 9. 缺少 API 请求限流

**问题描述**: 没有实现 Rate Limiting，AI 接口可能被滥用导致高额费用。

**所在位置**: `backend/src/main/java/com/tripweaver/controller/ChatController.java`

**建议修复方案**:
- 使用 Bucket4j 或 Resilience4j 实现限流
- 或在 Spring AI 调用层添加限流逻辑

---

### 10. TravelPlan 实体缺少 status 字段但前端使用了

**问题描述**: `TravelPlan` 实体没有 `status` 字段，但前端 `PlansView.vue` 和 `PlanDetailView.vue` 中使用了 `plan.status`。

**所在位置**:
- `backend/src/main/java/com/tripweaver/entity/TravelPlan.java`
- `frontend/src/views/PlansView.vue` 第 43-44 行

**建议修复方案**: 在 `TravelPlan` 实体中添加 `status` 字段：
```java
private String status = "draft"; // draft, planning, confirmed, completed
```

---

### 11. 缺少 @Transactional 注解

**问题描述**: 服务层方法没有使用 `@Transactional` 注解，可能导致数据一致性问题。

**所在位置**: `backend/src/main/java/com/tripweaver/service/ChatService.java` 的 `createNewPlan()` 方法

**建议修复方案**:
```java
@Transactional
public Long createNewPlan() {
    // 创建 TravelPlan 和 Conversation 应该在同一事务中
}
```

---

### 12. JWT Token 验证时未处理 Token 过期

**问题描述**: `JwtTokenProvider.validateToken()` 只返回 boolean，调用方无法区分具体错误类型（过期、格式错误等）。

**所在位置**: `backend/src/main/java/com/tripweaver/security/JwtTokenProvider.java` 第 48-58 行

**建议修复方案**:
- 在 Filter 中捕获特定异常
- 返回不同的 HTTP 状态码（401 for expired, 403 for invalid）

---

### 13. SearXNG 配置属性未设置默认值

**问题描述**: `SearXNGProperties` 的 `retryCount` 和 `retryDelayMs` 没有默认值，如果配置缺失会导致问题。

**所在位置**: `backend/src/main/java/com/tripweaver/config/SearXNGProperties.java` 第 16-18 行

**建议修复方案**:
```java
private int retryCount = 3;
private int retryDelayMs = 1000;
```

---

### 14. OpenAI API Key 配置有默认值

**问题描述**: OpenAI API Key 配置了默认的测试值，生产环境可能误用。

**所在位置**: `backend/src/main/resources/application.yml` 第 23 行

```yaml
api-key: ${OPENAI_API_KEY:sk-test-key}
```

**建议修复方案**: 移除默认值，强制配置环境变量。

---

### 15. 前端路由守卫安全性不足

**问题描述**: 仅检查 `isLoggedIn` 状态（localStorage 中是否有 token），没有验证 token 是否有效。

**所在位置**: `frontend/src/router/index.js` 第 46-54 行

**建议修复方案**:
- 在应用初始化时调用 `/api/auth/me` 验证 token
- 如果验证失败，清除本地认证信息并重定向到登录页

---

## 低优先级问题

### 16. 密码复杂度验证不足

**问题描述**: 注册时只验证密码长度至少 6 位，没有要求复杂度（大小写、数字、特殊字符）。

**所在位置**: `backend/src/main/java/com/tripweaver/dto/RegisterRequest.java` 第 15-16 行

**建议修复方案**:
```java
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
         message = "密码必须包含大小写字母和数字，至少8位")
private String password;
```

---

### 17. TravelPlan 缺少与 User 的外键关联

**问题描述**: `TravelPlan` 实体使用 `userId` 字段而非 JPA 关联关系，无法利用 JPA 的级联操作。

**所在位置**: `backend/src/main/java/com/tripweaver/entity/TravelPlan.java` 第 17-18 行

**建议修复方案**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

---

### 18. 缺少 API 文档

**问题描述**: 没有集成 Swagger/OpenAPI 等 API 文档工具。

**所在位置**: 整个后端项目

**建议修复方案**: 添加 `springdoc-openapi-starter-webmvc-ui` 依赖。

---

### 19. 测试覆盖不完整

**问题描述**: 缺少以下测试：
- `ChatService` 测试
- `PlanService` 测试
- `SearXNGClient` 集成测试
- 前端组件测试

**建议修复方案**: 补充单元测试和集成测试，提高覆盖率。

---

### 20. BCryptPasswordEncoder 未指定强度

**问题描述**: 使用默认构造函数创建 `BCryptPasswordEncoder`，未指定加密强度。

**所在位置**: `backend/src/main/java/com/tripweaver/config/SecurityConfig.java` 第 45 行

**建议修复方案**:
```java
return new BCryptPasswordEncoder(12); // 推荐强度 10-12
```

---

### 21. 前端缺少错误边界处理

**问题描述**: Vue 组件没有使用 `errorCaptured` 或全局错误处理器。

**建议修复方案**:
```javascript
// main.js
app.config.errorHandler = (err, vm, info) => {
  console.error('Global error:', err)
  // 可上报错误日志
}
```

---

### 22. 缺少日志记录规范

**问题描述**: 后端使用了 Lombok 的 `@Slf4j`，但日志记录不够统一和详细。

**建议修复方案**:
- 在关键业务操作（登录、创建计划、AI 调用）添加审计日志
- 使用 MDC 记录请求追踪 ID

---

### 23. 缺少健康检查端点

**问题描述**: 没有集成 Spring Boot Actuator 进行健康监控。

**建议修复方案**: 添加 `spring-boot-starter-actuator` 依赖并配置健康检查端点。

---

### 24. 前端缺少 Loading 状态的统一管理

**问题描述**: 每个组件各自管理 loading 状态，代码重复。

**建议修复方案**: 可以考虑使用 Pinia 统一管理全局加载状态，或封装可复用的 composable。

---

## 建议修复顺序

1. **XSS 漏洞** → 添加 DOMPurify 消毒
2. **全局异常处理器** → 统一错误响应
3. **CORS 配置** → 支持生产部署
4. **JSON 注入** → 使用 ObjectMapper 正确处理
5. **JWT/API Key 移除默认值** → 强制环境变量配置

---

# 功能改进分析报告

> 分析日期: 2026-05-09

## 一、AI 对话功能

### 1. 流式响应 - **缺失** ⭐ P0
- **当前状态**: 缺失
- **现状分析**: `AiService.chat()` 方法使用同步调用，用户发送消息后需要等待完整响应才能看到结果
- **改进建议**: 实现 SSE (Server-Sent Events) 或 WebSocket 流式响应，让 AI 生成的内容实时显示
- **预期价值**: 大幅提升用户体验，减少等待焦虑感，增强对话流畅度

### 2. 对话历史管理 - **不完善** ⭐ P3
- **当前状态**: 不完善
- **现状分析**:
  - `ChatService.updateConversationHistory()` 使用字符串拼接和 JSON 手动拼接
  - 历史传递方式是将历史字符串作为用户消息的一部分，而非使用 ChatClient 的内置历史管理
- **改进建议**:
  - 使用 JSON 库（如 Jackson）管理对话历史
  - 利用 Spring AI ChatClient 的 `Advisor` 或 `ChatMemory` 功能管理上下文
  - 实现对话历史的 token 限制，避免历史过长导致超出模型限制
- **预期价值**: 更可靠的对话历史管理，避免 JSON 解析错误，更合理的 token 消耗

### 3. 多工具调用 - **不完善** ⭐ P2
- **当前状态**: 不完善
- **现状分析**: 仅实现了 `WebSearchTool` 一个搜索工具
- **改进建议**: 添加更多实用工具：
  - 天气查询工具
  - 航班/火车查询工具
  - 酒店预订查询工具
  - 景点门票查询工具
  - 地图/路线规划工具
- **预期价值**: 提供更准确、更实时的旅行信息，提升计划的专业性和可信度

### 4. 提示词优化 - **可优化** ⭐ P2
- **当前状态**: 可优化
- **现状分析**: `SYSTEM_PROMPT` 内容全面但：
  - 缺少结构化输出格式要求
  - 缺少用户画像/偏好记忆机制
  - 未针对中文旅行场景做深度优化
- **改进建议**:
  - 要求 AI 输出 JSON 结构化数据，便于前端解析和展示
  - 添加用户偏好记忆，根据历史对话优化推荐
  - 增加中文语境下的旅行常识和注意事项
- **预期价值**: 更规范的输出格式，更个性化的推荐，更贴合中国用户习惯

---

## 二、旅行计划生成

### 1. 结构化计划存储 - **缺失** ⭐ P0
- **当前状态**: 缺失
- **现状分析**:
  - `TravelPlan` 实体仅有 `content` 一个文本字段
  - 无行程天数、预算、景点列表等结构化字段
  - 前端 `PlanDetailView.vue` 中期望的 `itinerary` 数组、`notes` 等字段在实体中不存在
- **改进建议**:
  - 扩展实体添加结构化字段：`budget`、`itineraryDays`、`hotelRecommendations`、`attractions` 等
  - 或将 `content` 改为存储完整 JSON 结构
  - AI 输出时要求生成对应的结构化 JSON
- **预期价值**: 前端可更好地展示计划详情，支持计划对比、编辑特定项目

### 2. 多日行程时间线 - **缺失** ⭐ P1
- **当前状态**: 缺失
- **现状分析**: 前端 `PlanDetailView.vue` 设计了时间线展示，但后端无对应数据结构
- **改进建议**:
  - 创建 `ItineraryDay` 实体，关联 `TravelPlan`
  - 每日行程包含 `date`、`title`、`activities` 列表
  - 添加 `Activity` 实体：`time`、`name`、`type`（景点/美食/交通等）、`location`、`description`
- **预期价值**: 生成真正可用的多日行程，用户可逐日查看和调整

### 3. 预算明细计算 - **缺失** ⭐ P2
- **当前状态**: 缺失
- **现状分析**: 系统提示词要求 AI 提供预算估算，但无预算字段和明细存储
- **改进建议**:
  - 添加 `Budget` 实体或字段：`totalAmount`、`transportCost`、`accommodationCost`、`foodCost`、`activityCost`、`otherCost`
  - AI 输出时估算各项费用
  - 前端展示预算明细饼图/列表
- **预期价值**: 用户可直观了解旅行费用，便于决策和规划

### 4. 景点/酒店推荐卡片 - **缺失** ⭐ P2
- **当前状态**: 缺失
- **现状分析**: AI 推荐仅以文本形式输出，无结构化存储
- **改进建议**:
  - 创建 `Recommendation` 实体：`type`、`name`、`description`、`rating`、`priceRange`、`location`、`link`
  - 前端以卡片形式展示推荐列表
  - 支持用户收藏/排除功能
- **预期价值**: 推荐内容更直观，用户可快速筛选和决策

---

## 三、搜索功能

### 1. 搜索结果结构化 - **缺失** ⭐ P2
- **当前状态**: 缺失
- **现状分析**: `SearXNGClient.search()` 返回原始 JSON 字符串，无结果解析、过滤、排序逻辑
- **改进建议**:
  - 解析 SearXNG 返回的 JSON，提取标题、链接、摘要、来源
  - 过滤无关结果，按相关性排序
  - 返回结构化的搜索结果对象给 AI
- **预期价值**: AI 可更精准使用搜索结果，提升生成内容准确性

### 2. 搜索缓存 - **缺失** ⭐ P4
- **当前状态**: 缺失
- **现状分析**: 每次搜索都直接调用 SearXNG API，无缓存机制
- **改进建议**:
  - 对高频查询结果进行缓存（如热门城市天气、常见景点信息）
  - 设置缓存过期时间（如天气数据缓存 1 小时）
- **预期价值**: 减少外部 API 调用，提升响应速度，降低依赖风险

### 3. 搜索历史记录 - **缺失** ⭐ P4
- **当前状态**: 缺失
- **改进建议**:
  - 创建 `SearchHistory` 实体，记录用户搜索关键词和时间
  - 前端展示最近搜索，便于快速复用
- **预期价值**: 用户可回顾搜索历史，了解信息来源

---

## 四、用户管理

### 1. 用户资料完善 - **缺失** ⭐ P3
- **当前状态**: 缺失
- **现状分析**: `User` 实体仅有 `username`、`password`、`email`，无头像、昵称、联系方式等
- **改进建议**:
  - 添加字段：`avatarUrl`、`nickname`、`phone`、`bio`（个人简介）
  - 前端支持头像上传和资料编辑
- **预期价值**: 用户有归属感，支持个性化展示

### 2. 旅行偏好设置 - **缺失** ⭐ P2
- **当前状态**: 缺失
- **改进建议**:
  - 创建 `UserPreference` 实体：`preferredDestinations`、`budgetLevel`、`travelStyle`（休闲/探险/文化等）、`foodPreference` 等
  - AI 生成计划时参考用户偏好
- **预期价值**: AI 推荐更个性化，减少无效建议

### 3. 用户头像上传 - **缺失** ⭐ P3
- **当前状态**: 缺失
- **改进建议**: 实现头像上传 API 和存储（可使用本地存储或云存储）
- **预期价值**: 增强用户个人形象，提升社区感

---

## 五、数据持久化

### 1. 计划版本管理 - **缺失** ⭐ P3
- **当前状态**: 缺失
- **现状分析**: 用户修改计划会直接覆盖，无历史版本
- **改进建议**:
  - 创建 `PlanVersion` 实体，每次保存生成新版本
  - 支持查看和恢复历史版本
- **预期价值**: 用户可追溯计划变更，防止误操作

### 2. 计划导出功能 - **缺失** ⭐ P2
- **当前状态**: 缺失
- **改进建议**:
  - 支持导出为 PDF、Word、Markdown 格式
  - 支持分享为图片或网页链接
- **预期价值**: 用户可离线保存计划，便于分享给同行者

### 3. 计划分享功能 - **缺失** ⭐ P2
- **当前状态**: 缺失
- **改进建议**:
  - 创建分享链接机制
  - 支持设置分享权限（公开/仅好友/私密）
  - 支持协作编辑（邀请好友一起规划）
- **预期价值**: 增强社交属性，便于多人旅行规划协作

---

## 六、前端交互

### 1. Toast/通知组件 - **缺失** ⭐ P1
- **当前状态**: 缺失
- **现状分析**: 使用 `alert()` 和 `confirm()` 原生弹窗
- **改进建议**:
  - 创建 Toast 组件统一展示成功/错误/警告消息
  - 创建 ConfirmDialog 组件替代原生 confirm
- **预期价值**: 更统一的视觉体验，更友好的交互反馈

### 2. 加载状态完善 - **可优化** ⭐ P3
- **当前状态**: 可优化
- **现状分析**: 部分页面有 loading spinner，但缺少骨架屏
- **改进建议**:
  - 实现骨架屏替代简单 spinner
  - 添加加载进度提示（特别是 AI 响应时）
- **预期价值**: 减少视觉跳变，提升感知速度

### 3. 响应式设计完善 - **可优化** ⭐ P4
- **当前状态**: 可优化
- **现状分析**:
  - ChatView 的侧边栏直接隐藏，无替代导航
  - 小屏幕下的对话体验不够优化
- **改进建议**:
  - 小屏幕下侧边栏改为可滑出/收起的 Drawer
  - 优化移动端的输入区域和消息展示
- **预期价值**: 更好的移动端体验，扩大用户群体

### 4. 错误边界处理 - **缺失** ⭐ P4
- **当前状态**: 缺失
- **现状分析**: API 错误仅用 `console.error` 和简单提示
- **改进建议**:
  - 实现全局错误处理机制
  - 添加网络错误重试机制
  - 展示友好的错误页面
- **预期价值**: 提升系统稳定性，减少用户困惑

---

## 七、缺失的核心功能（对比竞品）

### 1. 行程地图可视化 - **缺失** ⭐ P1
- **当前状态**: 缺失
- **竞品对比**: 携程、飞猪等都有地图展示行程路线
- **改进建议**:
  - 集成地图服务（如高德/百度地图）
  - 在地图上标注每日行程点位
  - 展示路线规划
- **预期价值**: 直观展示行程，用户可快速理解空间关系

### 2. 实时预订对接 - **缺失** ⭐ P1
- **当前状态**: 缺失
- **竞品对比**: 携程/飞猪可直接预订机票、酒店、门票
- **改进建议**:
  - 接入机票/酒店/门票预订 API
  - 或提供跳转到预订平台的链接
  - 记录预订状态（已预订/待预订）
- **预期价值**: 从规划到预订的闭环，提升实用价值

### 3. 行程提醒/日历同步 - **缺失** ⭐ P3
- **当前状态**: 缺失
- **竞品对比**: 多数旅行 App 支持行程提醒和日历导入
- **改进建议**:
  - 生成日历事件文件（.ics）
  - 支持导入到 Google Calendar/Apple Calendar/微信日历
  - 发送出发前提醒通知
- **预期价值**: 用户不会遗忘行程，提升可靠性

### 4. 用户评价/反馈系统 - **缺失** ⭐ P3
- **当前状态**: 缺失
- **竞品对比**: 携程等有用户点评帮助决策
- **改进建议**:
  - 支持用户对 AI 推荐的景点/酒店评分
  - 收集用户对计划质量的反馈
  - 根据反馈优化 AI 推荐
- **预期价值**: 持续改进系统，建立用户信任

### 5. 旅行攻略库 - **缺失** ⭐ P3
- **当前状态**: 缺失
- **竞品对比**: 马蜂窝、穷游有丰富的攻略内容
- **改进建议**:
  - 建立目的地攻略数据库
  - AI 生成计划时参考攻略内容
  - 支持用户贡献攻略
- **预期价值**: 内容更丰富，减少 AI 生成的不确定性

### 6. 多语言支持 - **缺失** ⭐ P4
- **当前状态**: 缺失
- **竞品对比**: 携程等支持多语言
- **改进建议**:
  - 前端国际化（i18n）
  - AI 支持输出不同语言
- **预期价值**: 扩大潜在用户群体

### 7. 客服/帮助系统 - **缺失** ⭐ P4
- **当前状态**: 缺失
- **竞品对比**: 商业化产品都有客服支持
- **改进建议**:
  - 添加常见问题 FAQ
  - 添加使用指南
  - 支持反馈提交入口
- **预期价值**: 降低用户学习成本，提升满意度

---

## 功能改进优先级排序

| 优先级 | 功能 | 当前状态 | 预期价值 |
|--------|------|----------|----------|
| P0 | 流式响应 | 缺失 | 核心体验提升 |
| P0 | 结构化计划存储 | 缺失 | 数据展示基础 |
| P1 | 多日行程时间线 | 缺失 | 实用性核心 |
| P1 | Toast/通知组件 | 缺失 | 交互体验统一 |
| P1 | 行程地图可视化 | 缺失 | 直观展示行程 |
| P1 | 实时预订对接 | 缺失 | 闭环体验 |
| P2 | 多工具调用扩展 | 不完善 | 信息准确性 |
| P2 | 预算明细计算 | 缺失 | 决策辅助 |
| P2 | 搜索结果结构化 | 缺失 | AI 信息质量 |
| P2 | 用户偏好设置 | 缺失 | 个性化推荐 |
| P2 | 计划导出/分享 | 缺失 | 社交与保存 |
| P3 | 对话历史优化 | 不完善 | 稳定性提升 |
| P3 | 用户资料完善 | 缺失 | 用户归属感 |
| P3 | 骨架屏加载 | 可优化 | 视觉体验 |
| P3 | 计划版本管理 | 缺失 | 安全备份 |
| P3 | 行程提醒/日历 | 缺失 | 可靠性提升 |
| P4 | 搜索缓存 | 缺失 | 性能优化 |
| P4 | 响应式完善 | 可优化 | 移动端体验 |
| P4 | 错误边界处理 | 缺失 | 系统稳定性 |

---

## 关键代码位置参考

| 模块 | 文件路径 |
|------|----------|
| AI 服务 | `backend/src/main/java/com/tripweaver/ai/AiService.java` |
| 对话服务 | `backend/src/main/java/com/tripweaver/service/ChatService.java` |
| 计划实体 | `backend/src/main/java/com/tripweaver/entity/TravelPlan.java` |
| 用户实体 | `backend/src/main/java/com/tripweaver/entity/User.java` |
| 搜索工具 | `backend/src/main/java/com/tripweaver/tools/WebSearchTool.java` |
| 搜索客户端 | `backend/src/main/java/com/tripweaver/client/SearXNGClient.java` |
| 对话页面 | `frontend/src/views/ChatView.vue` |
| 计划详情页 | `frontend/src/views/PlanDetailView.vue` |
| 计划列表页 | `frontend/src/views/PlansView.vue` |
| 用户状态 | `frontend/src/stores/user.js` |

---

# Spring AI 最佳实践改进建议

> 基于 Spring AI 1.1.6 官方文档分析

## 一、ChatClient API 改进

### 1. 使用 ChatClient Builder 模式 - **当前未使用**

**当前状态**: TripWeaver 直接使用 `ChatModel` 而非 `ChatClient`

**改进建议**: 迁移到 `ChatClient` API，获得以下优势：
- 流式响应支持 (`stream()`)
- 结构化输出 (`entity()`)
- Advisor 链式配置
- 默认系统提示词配置

```java
// 推荐方式
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultSystemPrompt("你是一个旅行规划助手...")
    .defaultAdvisors(
        MessageChatMemoryAdvisor.builder(chatMemory).build()
    )
    .build();

// 流式响应
Flux<String> response = chatClient.prompt()
    .user(userMessage)
    .stream()
    .content();
```

### 2. 流式响应实现 - **缺失** ⭐ P0

**当前状态**: 使用同步 `chatModel.call()` 阻塞等待完整响应

**改进建议**: 使用 `stream()` 实现实时响应

```java
// Controller 层
@GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamChat(@RequestParam String message) {
    return chatClient.prompt()
        .user(message)
        .stream()
        .content();
}
```

### 3. 结构化输出 - **缺失** ⭐ P0

**当前状态**: AI 输出纯文本，前端难以解析

**改进建议**: 使用 `entity()` 或 `BeanOutputConverter` 获取结构化数据

```java
// 定义旅行计划结构
record TravelPlanResponse(
    String title,
    List<DayItinerary> itinerary,
    BudgetInfo budget,
    List<Recommendation> recommendations
) {}

// 使用 ChatClient 获取结构化输出
TravelPlanResponse plan = chatClient.prompt()
    .user("规划成都到重庆的两日游")
    .call()
    .entity(TravelPlanResponse.class);

// 或使用原生结构化输出（更可靠）
TravelPlanResponse plan = chatClient.prompt()
    .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
    .user("规划成都到重庆的两日游")
    .call()
    .entity(TravelPlanResponse.class);
```

---

## 二、Chat Memory 改进

### 1. 使用 MessageChatMemoryAdvisor - **当前未使用**

**当前状态**: 手动拼接 JSON 字符串管理对话历史

**改进建议**: 使用 Spring AI 内置的 Chat Memory

```java
// 配置 ChatMemory（支持多种存储）
ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(jdbcChatMemoryRepository)  // JDBC 持久化
    .maxMessages(20)  // 保留最近 20 条消息
    .build();

// 配置 ChatClient
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
    .build();

// 调用时指定会话 ID
String response = chatClient.prompt()
    .user("帮我规划旅行")
    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
    .call()
    .content();
```

### 2. ChatMemoryRepository 选择

| 存储类型 | 适用场景 | 依赖 |
|---------|---------|------|
| `InMemoryChatMemoryRepository` | 开发测试 | 无 |
| `JdbcChatMemoryRepository` | 生产环境，关系数据库 | `spring-ai-starter-model-chat-memory-repository-jdbc` |
| `MongoChatMemoryRepository` | MongoDB 环境 | `spring-ai-starter-model-chat-memory-repository-mongodb` |
| `Neo4jChatMemoryRepository` | 图数据库环境 | `spring-ai-starter-model-chat-memory-repository-neo4j` |

**推荐**: TripWeaver 使用 SQLite，选择 `JdbcChatMemoryRepository`

---

## 三、Advisors API 改进

### 1. 内置 Advisors - **当前未使用**

Spring AI 提供多种内置 Advisor：

| Advisor | 用途 |
|---------|------|
| `MessageChatMemoryAdvisor` | 对话历史管理 |
| `QuestionAnswerAdvisor` | RAG 检索增强 |
| `RetrievalAugmentationAdvisor` | 高级 RAG 流程 |
| `SimpleLoggerAdvisor` | 日志记录 |
| `ReReadingAdvisor` | 提升推理能力 |
| `SafeGuardAdvisor` | 内容安全过滤 |

### 2. RAG 增强 - **可优化**

**当前状态**: 仅使用 `WebSearchTool` 进行简单搜索

**改进建议**: 使用 `QuestionAnswerAdvisor` 或 `RetrievalAugmentationAdvisor`

```java
// 简单 RAG
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder()
            .similarityThreshold(0.7)
            .topK(5)
            .build())
        .build())
    .build();

// 高级 RAG（支持查询重写、多查询扩展）
Advisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
    .queryTransformers(RewriteQueryTransformer.builder()
        .chatClientBuilder(chatClientBuilder)
        .build())
    .documentRetriever(VectorStoreDocumentRetriever.builder()
        .vectorStore(vectorStore)
        .similarityThreshold(0.5)
        .build())
    .build();
```

### 3. 自定义 Advisor

可以实现自定义 Advisor 进行：
- 请求/响应日志记录
- 敏感信息过滤
- 用户偏好注入
- Token 统计

```java
public class UserPreferenceAdvisor implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        // 注入用户偏好到系统提示词
        String preferences = loadUserPreferences();
        return request.mutate()
            .prompt(request.prompt().augmentSystemMessage(preferences))
            .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }
}
```

---

## 四、Tool Calling 改进

### 1. 使用 @Tool 注解 - **当前方式可优化**

**当前状态**: 实现 `ToolCallback` 接口

**改进建议**: 使用声明式 `@Tool` 注解更简洁

```java
@Component
public class TravelTools {

    @Tool(description = "搜索旅行相关信息")
    public String searchWeb(
        @ToolParam(description = "搜索关键词") String query,
        @ToolParam(description = "搜索类型", required = false) String type
    ) {
        // 实现搜索逻辑
    }

    @Tool(description = "查询天气信息")
    public WeatherInfo getWeather(
        @ToolParam(description = "城市名称") String city,
        @ToolParam(description = "日期，格式 yyyy-MM-dd") String date
    ) {
        // 实现天气查询
    }
}

// 使用
chatClient.prompt()
    .user("帮我查一下成都明天的天气")
    .tools(new TravelTools())
    .call()
    .content();
```

### 2. 添加更多工具 - **建议扩展**

| 工具 | 用途 | 优先级 |
|------|------|--------|
| 天气查询 | 获取目的地天气 | P1 |
| 航班查询 | 查询航班信息 | P2 |
| 酒店查询 | 查询酒店信息 | P2 |
| 景点查询 | 查询景点详情 | P2 |
| 地图路线 | 路线规划 | P2 |

### 3. Tool Context - **可用于多租户**

```java
@Tool(description = "获取用户旅行偏好")
public UserPreference getUserPreferences(Long userId, ToolContext context) {
    String tenantId = context.getContext().get("tenantId");
    return preferenceService.getByUserAndTenant(userId, tenantId);
}

// 调用时传入上下文
chatClient.prompt()
    .user("推荐适合我的旅行计划")
    .tools(new TravelTools())
    .toolContext(Map.of("tenantId", "acme"))
    .call()
    .content();
```

---

## 五、Prompt 模板改进

### 1. 使用 PromptTemplate - **当前硬编码**

**当前状态**: 系统提示词硬编码在字符串常量中

**改进建议**: 使用模板引擎支持变量替换

```java
// 使用模板
String response = chatClient.prompt()
    .user(u -> u.text("""
        为我计划{date}从{departure}出发，前往{destination}的{duration}旅游计划，预算{budget}元。
        请考虑以下用户偏好：{preferences}
        """)
        .param("date", "5月15日")
        .param("departure", "成都")
        .param("destination", "重庆")
        .param("duration", "两日")
        .param("budget", "2000")
        .param("preferences", userPreferences))
    .call()
    .content();
```

### 2. 自定义模板分隔符

避免与 JSON 冲突：

```java
.templateRenderer(StTemplateRenderer.builder()
    .startDelimiterToken('<')
    .endDelimiterToken('>')
    .build())
```

---

## 六、Observability 改进

### 1. 启用日志 Advisor

```java
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(new SimpleLoggerAdvisor())
    .build();

// 配置日志级别
// logging.level.org.springframework.ai.chat.client.advisor=DEBUG
```

### 2. Micrometer 集成

Spring AI 自动支持 Micrometer 观测：
- Token 使用统计
- 请求延迟
- 工具调用追踪

---

## 七、MCP (Model Context Protocol) 支持 - **新特性**

Spring AI 1.1.6 支持 MCP，可以：
- 连接外部 MCP 服务器获取工具
- 实现 MCP 服务器暴露工具

```yaml
# 配置 MCP 客户端
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            travel-tools:
              url: http://mcp-server:8080
```

---

## 改进优先级（基于 Spring AI 最佳实践）

| 优先级 | 改进项 | 当前状态 | Spring AI 特性 |
|--------|--------|----------|----------------|
| P0 | 迁移到 ChatClient API | 使用 ChatModel | ChatClient |
| P0 | 实现流式响应 | 同步阻塞 | `stream()` |
| P0 | 结构化输出 | 纯文本 | `entity()` |
| P1 | Chat Memory | 手动 JSON 拼接 | MessageChatMemoryAdvisor |
| P1 | 使用 @Tool 注解 | 实现 ToolCallback | 声明式工具 |
| P2 | RAG Advisor | 简单搜索 | QuestionAnswerAdvisor |
| P2 | 添加更多工具 | 仅 WebSearch | @Tool 注解 |
| P3 | Prompt 模板 | 硬编码 | PromptTemplate |
| P3 | 日志 Advisor | 无 | SimpleLoggerAdvisor |
| P4 | MCP 支持 | 无 | MCP Client |
