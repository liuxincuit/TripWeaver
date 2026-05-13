# CLAUDE.md

## 项目概述

TripWeaver 后端 - 基于 Spring Boot 的 AI 旅行规划服务。

## 常用命令

```bash
mvn spring-boot:run                              # 启动开发服务器 (端口 8080)
mvn clean test                                   # 运行所有测试
mvn test -Dtest=ClassName                        # 运行单个测试类
mvn test -Dtest=ClassName#methodName             # 运行单个测试方法
mvn test -Dtest=ClassName#methodName -q          # 运行单个测试方法 (安静模式)
```

## 技术栈

- Java 21 + Spring Boot 3.5.14 + Spring AI 1.1.6
- AI: Spring AI OpenAI (可配置其他模型)

## 架构要点

### 包结构

```
com.tripweaver/
├── controller/    # REST API (AuthController, ChatController, PlanController)
├── service/       # 业务逻辑 (UserService, ChatService, PlanService, AiService)
├── entity/        # JPA 实体 (User, TravelPlan, Conversation)
├── repository/    # 数据访问
├── security/      # JWT 认证 (JwtTokenProvider, JwtAuthenticationFilter)
├── dto/           # 数据传输对象
├── ai/            # AI 服务集成 (AiService - ChatClient 封装)
├── tools/         # AI 工具定义 (WebSearchTool - SearXNG 搜索)
├── client/        # 外部服务客户端 (SearXNGClient)
└── config/        # 配置类 (SecurityConfig, AiConfig, SearXNGProperties)
```

### 环境配置

通过 `.env` 文件配置 (dotenv-java 加载):

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `OPENAI_API_KEY` | AI 模型 API Key | - |
| `JWT_SECRET` | JWT 签名密钥 (≥32字符) | - |
| `SEARXNG_BASE_URL` | SearXNG 搜索服务地址 | - |
| `SPRING_AI_OPENAI_BASE_URL` | API 基础 URL | `https://api.openai.com` |
| `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL` | 模型名称 | `gpt-4o` |

### 数据库

- 生产: SQLite (`./data/tripweaver.db`)，使用 Hibernate 社区方言
- 测试: H2 内存数据库 (`application-test.yml`)
- DDL: 生产 `update`，测试 `create-drop`

## 设计决策

### 计划删除与聊天记忆清理

删除计划时采用**最终一致性**设计：

- 计划删除操作在数据库事务中执行
- 聊天记忆清理（`chatMemory.clear()`）在事务提交后执行
- 如果聊天记忆清理失败，仅记录警告日志，不影响删除操作成功

**设计理由**：
- ChatMemory 是外部存储（Redis/内存），其故障不应阻止用户删除计划
- 孤立的聊天记忆不影响系统功能，可通过定期清理任务处理
- 保证删除操作的可用性比强一致性更重要

## 依赖管理

遵循 `.claude/rules/maven-dependencies.md`
