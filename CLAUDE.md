# CLAUDE.md

## 项目概述

TripWeaver 是基于 AI 的旅行规划系统。用户通过自然语言对话描述需求，系统生成完整旅行计划。

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

### e2e 测试参考

`.claude\rules\e2e-testing.md`

## Agent skills

### Issue tracker

Issues are tracked in GitHub Issues. See `docs/agents/issue-tracker.md`.

### Triage labels

Using default triage labels: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout: one `CONTEXT.md` and `docs/adr/` at repo root. See `docs/agents/domain.md`.