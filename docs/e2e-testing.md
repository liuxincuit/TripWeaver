# TripWeaver 端到端测试指南

## 快速开始

```bash
# 1. 启动后端服务
cd backend && mvn spring-boot:run

# 2. 运行 E2E 测试（新终端）
cd frontend && npm run test:e2e

# 带 UI 运行
npm run test:e2e:ui
```

## 测试文件位置

```
frontend/src/e2e/
├── auth.spec.ts    # 认证流程测试
└── chat.spec.ts    # AI 对话测试
```

## 测试覆盖

| 测试用例 | 说明 |
|---------|------|
| 用户注册成功 | 注册新用户并验证跳转首页 |
| 用户登录成功 | 登录并验证跳转首页 |
| 登录失败显示错误信息 | 使用错误凭据验证错误提示 |
| 新建对话并发送消息 | 测试 AI 对话功能 |

## 添加新测试

1. 在 `frontend/src/e2e/` 创建 `.spec.ts` 文件
2. 使用 `data-testid` 选择器定位元素：

```typescript
import { test, expect } from '@playwright/test';

test('示例测试', async ({ page }) => {
  await page.goto('/your-page');
  await page.getByTestId('your-element').click();
  await expect(page.getByTestId('result')).toBeVisible();
});
```

## 现有 data-testid

| 页面 | data-testid |
|------|-------------|
| 注册 | `username-input`, `email-input`, `password-input`, `register-button`, `error-message`, `login-link` |
| 登录 | `username-input`, `password-input`, `login-button`, `error-message`, `register-link` |
| 首页 | `new-plan-button` |
| 聊天 | `chat-input`, `send-button`, `message-list`, `user-message`, `assistant-message`, `loading-indicator` |

## 注意事项

- 运行测试前必须启动后端服务
- 测试使用随机用户名避免数据冲突
- AI 对话测试最长等待 60 秒
