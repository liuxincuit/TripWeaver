# CLAUDE.md

## 项目概述

TripWeaver 是基于 AI 的旅行规划系统。用户通过自然语言对话描述需求，系统生成完整旅行计划。

## 常用命令

### 后端 (backend/)
```bash
mvn spring-boot:run          # 启动开发服务器
mvn test                     # 运行所有测试
mvn test -Dtest=ClassName    # 运行单个测试类
mvn test -Dtest=ClassName#methodName  # 运行单个测试方法
```

### 前端 (frontend/)
```bash
npm run dev        # 启动开发服务器
npm run build      # 构建生产版本
npm run test       # 运行测试 (watch 模式)
npm run test:run   # 运行测试 (单次)
npm run test:e2e   # 运行端到端测试 (需先启动后端)
npm run test:e2e:ui # 带 UI 运行端到端测试
```

## 技术栈

- 后端: Java 21 + Spring Boot 3.5.14 + Spring AI 1.1.5
- 前端: Vue 3 + Vite + Pinia + Vue Router
- 数据库: SQLite (生产) / H2 (测试)
- AI: Spring AI OpenAI (可配置其他模型)

## 架构要点

### 后端包结构
```
com.tripweaver/
├── controller/    # REST API
├── service/       # 业务逻辑
├── entity/        # JPA 实体 (User, TravelPlan, Conversation)
├── repository/    # 数据访问
├── security/      # JWT 认证
├── dto/           # 数据传输对象
├── ai/            # AI 服务集成
├── tools/         # AI 工具定义
└── config/        # 配置类
```

### 前端路由
- `/login`, `/register` - 认证页面
- `/` - 首页
- `/chat/:planId` - AI 对话页
- `/plans`, `/plan/:id` - 计划列表与详情

### 认证机制
JWT Token 认证，前端存储于 Pinia store，请求时通过 Authorization header 传递。

### 环境配置

复制 `.env.example` 为 `.env` 并填入配置：

```bash
cp .env.example .env
```

必需配置：
- `OPENAI_API_KEY` - AI 模型 API Key
- `JWT_SECRET` - JWT 签名密钥（至少32字符）
- `SEARXNG_BASE_URL` - SearXNG 搜索服务地址

可选配置（用于非 OpenAI 模型）：
- `SPRING_AI_OPENAI_BASE_URL` - API 基础 URL
- `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL` - 模型名称

### 数据库
- 生产环境: SQLite (`./data/tripweaver.db`)
- 测试环境: H2 内存数据库

## 测试约定

后端使用 Spring Boot Test + Spring Security Test，前端使用 Vitest + Vue Test Utils。

### E2E 测试约定

- 使用 Playwright 进行端到端测试
- 测试文件位于 `frontend/src/e2e/`
- 关键元素使用 `data-testid` 属性定位
- 运行 E2E 测试前需启动后端服务
