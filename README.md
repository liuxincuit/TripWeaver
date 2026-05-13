# TripWeaver

基于 AI 的旅行规划系统。用户通过自然语言对话描述需求，系统生成完整旅行计划。

## 快速开始

### 1. 环境配置

```bash
cp .env.example .env
```

编辑 `.env` 填入配置：

```env
OPENAI_API_KEY=your-api-key
JWT_SECRET=your-secret-at-least-32-characters
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

## 技术栈

- 后端: Java 21 + Spring Boot 3.5.14 + Spring AI 1.1.6
- 前端: Vue 3 + Vite + Pinia + Vue Router
- 数据库: SQLite

## 配置说明

| 变量 | 说明 | 必需 |
|------|------|------|
| `OPENAI_API_KEY` | AI 模型 API Key | 是 |
| `JWT_SECRET` | JWT 签名密钥 | 是 |
| `SEARXNG_BASE_URL` | SearXNG 搜索服务地址 | 是 |
| `SPRING_AI_OPENAI_BASE_URL` | API 基础 URL | 否 |
| `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL` | 模型名称 | 否 |

## 测试

```bash
# 后端
cd backend && mvn clean test

# 前端
cd frontend && npm run test:run
```