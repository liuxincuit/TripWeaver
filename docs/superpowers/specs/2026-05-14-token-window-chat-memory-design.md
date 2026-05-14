# TokenWindowChatMemory 设计文档

## 问题

当前 `MessageWindowChatMemory` 仅按消息数量限制窗口（默认 20 条），不考虑 token 数量。当对话消息较长时，可能导致 token 数量超出模型限制，引发 API 调用失败。

## 方案

实现 `TokenWindowChatMemory`，同时支持 **token 数量** 和 **消息数量** 双维度限制，超限时从最早的非 SystemMessage 开始逐条移除。

## 配置

| 环境变量 | yml 路径 | 默认值 |
|---|---|---|
| `AI_CHAT_MEMORY_MAX_TOKENS` | `spring.ai.chat.memory.token-window.max-tokens` | `100000` |
| `AI_CHAT_MEMORY_MAX_MESSAGES` | `spring.ai.chat.memory.token-window.max-messages` | `20` |

`@Value` 注解自动支持环境变量覆盖。

## 核心逻辑

```
add(conversationId, newMessages):
  1. 从 repository 取出已有消息
  2. 合并：已有消息 + 新消息，去重 SystemMessage
  3. 裁剪：从最早的非 SystemMessage 逐条移除，直到：
     - 总 token 数 ≤ maxTokens
     - 且消息数 ≤ maxMessages
  4. 将裁剪后的消息写回 repository
```

## Token 计数

使用 Spring AI 内置的 `JTokkitTokenCountEstimator`（默认 `cl100k_base` 编码），通过 `message.getText()` 获取内容文本进行估算。SystemMessage 也计入 token 预算，但永远不会被裁剪。

## 文件变更

### 新增

- `backend/src/main/java/com/tripweaver/chat/memory/TokenWindowChatMemory.java` — 自定义 ChatMemory 实现

### 修改

- `backend/src/main/java/com/tripweaver/config/AiConfig.java` — ChatMemory bean 替换为 TokenWindowChatMemory
- `backend/src/test/java/com/tripweaver/config/TestAiConfig.java` — 同理替换
- `backend/src/main/resources/application.yml` — 新增 token-window 配置项

## 集成方式

自定义 `ChatMemory` bean 覆盖 Spring AI 自动配置的 `MessageWindowChatMemory`，注入同一个 `ChatMemoryRepository`（生产：JdbcChatMemoryRepository，测试：InMemoryChatMemoryRepository），不影响持久化层。

## 测试要点

- 已有的 `ChatServiceTest`、`PlanServiceTest`、`AiServiceTest` 均 mock `ChatMemory`，不受影响
- 新增 `TokenWindowChatMemoryTest`：验证 token 超限裁剪、消息数量超限裁剪、SystemMessage 不被移除、空消息、单条超大消息等边界情况
