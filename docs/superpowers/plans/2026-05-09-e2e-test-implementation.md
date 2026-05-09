# TripWeaver 端到端测试实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 TripWeaver 添加 Playwright 端到端测试，自动化验证用户注册、登录、AI 对话等核心流程。

**Architecture:** 使用 Playwright 作为 E2E 测试框架，通过 data-testid 语义化选择器定位元素，测试脚本放在 frontend/src/e2e/ 目录下。

**Tech Stack:** Playwright, TypeScript, Vitest

---

## 文件结构

```
frontend/
├── src/
│   ├── views/
│   │   ├── RegisterView.vue    # 添加 data-testid
│   │   ├── LoginView.vue       # 添加 data-testid
│   │   ├── HomeView.vue        # 添加 data-testid
│   │   └── ChatView.vue        # 添加 data-testid
│   └── e2e/                    # 新建目录
│       ├── auth.spec.ts        # 认证流程测试
│       └── chat.spec.ts        # AI 对话测试
├── playwright.config.ts        # 新建：Playwright 配置
└── package.json                # 修改：添加 Playwright 依赖
```

---

### Task 1: 添加 Playwright 依赖

**Files:**
- Modify: `frontend/package.json`

- [ ] **Step 1: 添加 Playwright 依赖到 package.json**

在 `frontend/package.json` 中添加 Playwright 相关依赖和脚本：

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "test": "vitest",
    "test:run": "vitest run",
    "test:e2e": "playwright test",
    "test:e2e:ui": "playwright test --ui"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^6.0.6",
    "@vue/test-utils": "^2.4.0",
    "jsdom": "^29.1.1",
    "vite": "^8.0.11",
    "vitest": "^4.1.5",
    "@playwright/test": "^1.40.0"
  }
}
```

- [ ] **Step 2: 安装依赖**

Run: `cd frontend && npm install`
Expected: 依赖安装成功

- [ ] **Step 3: 安装 Playwright 浏览器**

Run: `cd frontend && npx playwright install chromium`
Expected: Chromium 浏览器安装成功

---

### Task 2: 创建 Playwright 配置

**Files:**
- Create: `frontend/playwright.config.ts`

- [ ] **Step 1: 创建 playwright.config.ts**

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './src/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
});
```

- [ ] **Step 2: 验证配置文件语法**

Run: `cd frontend && npx playwright test --list`
Expected: 显示测试文件列表（此时为空，但不应报错）

---

### Task 3: 为注册页面添加 data-testid

**Files:**
- Modify: `frontend/src/views/RegisterView.vue`

- [ ] **Step 1: 为用户名输入框添加 data-testid**

在 `RegisterView.vue` 第 26-32 行，修改用户名输入框：

```html
<input
  v-model="form.username"
  type="text"
  class="input"
  placeholder="请输入用户名"
  required
  data-testid="username-input"
/>
```

- [ ] **Step 2: 为邮箱输入框添加 data-testid**

在 `RegisterView.vue` 第 43-50 行，修改邮箱输入框：

```html
<input
  v-model="form.email"
  type="email"
  class="input"
  placeholder="请输入邮箱地址"
  required
  data-testid="email-input"
/>
```

- [ ] **Step 3: 为密码输入框添加 data-testid**

在 `RegisterView.vue` 第 60-68 行，修改密码输入框：

```html
<input
  v-model="form.password"
  type="password"
  class="input"
  placeholder="请输入密码（至少6位）"
  required
  minlength="6"
  data-testid="password-input"
/>
```

- [ ] **Step 4: 为注册按钮添加 data-testid**

在 `RegisterView.vue` 第 81-84 行，修改注册按钮：

```html
<button type="submit" class="btn btn-accent btn-block" :disabled="loading" data-testid="register-button">
  <span v-if="loading" class="loading-spinner"></span>
  <span>{{ loading ? '注册中...' : '注册账户' }}</span>
</button>
```

- [ ] **Step 5: 为错误提示添加 data-testid**

在 `RegisterView.vue` 第 72-79 行，修改错误提示：

```html
<div v-if="error" class="error-message" data-testid="error-message">
  <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8" x2="12" y2="12"/>
    <line x1="12" y1="16" x2="12.01" y2="16"/>
  </svg>
  {{ error }}
</div>
```

- [ ] **Step 6: 为登录链接添加 data-testid**

在 `RegisterView.vue` 第 88-89 行，修改登录链接：

```html
<router-link to="/login" class="link-accent" data-testid="login-link">立即登录</router-link>
```

---

### Task 4: 为登录页面添加 data-testid

**Files:**
- Modify: `frontend/src/views/LoginView.vue`

- [ ] **Step 1: 为用户名输入框添加 data-testid**

在 `LoginView.vue` 第 65-72 行，修改用户名输入框：

```html
<input
  v-model="form.username"
  type="text"
  class="input"
  placeholder="请输入用户名"
  required
  data-testid="username-input"
/>
```

- [ ] **Step 2: 为密码输入框添加 data-testid**

在 `LoginView.vue` 第 82-89 行，修改密码输入框：

```html
<input
  v-model="form.password"
  type="password"
  class="input"
  placeholder="请输入密码"
  required
  data-testid="password-input"
/>
```

- [ ] **Step 3: 为登录按钮添加 data-testid**

在 `LoginView.vue` 第 101-104 行，修改登录按钮：

```html
<button type="submit" class="btn btn-primary btn-block" :disabled="loading" data-testid="login-button">
  <span v-if="loading" class="loading-spinner"></span>
  <span>{{ loading ? '登录中...' : '登录' }}</span>
</button>
```

- [ ] **Step 4: 为错误提示添加 data-testid**

在 `LoginView.vue` 第 92-99 行，修改错误提示：

```html
<div v-if="error" class="error-message" data-testid="error-message">
  <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8" x2="12" y2="12"/>
    <line x1="12" y1="16" x2="12.01" y2="16"/>
  </svg>
  {{ error }}
</div>
```

- [ ] **Step 5: 为注册链接添加 data-testid**

在 `LoginView.vue` 第 109 行，修改注册链接：

```html
<router-link to="/register" class="link-accent" data-testid="register-link">立即注册</router-link>
```

---

### Task 5: 为首页添加 data-testid

**Files:**
- Modify: `frontend/src/views/HomeView.vue`

- [ ] **Step 1: 为新建计划按钮添加 data-testid**

在 `HomeView.vue` 第 37-42 行，修改新建计划按钮：

```html
<button @click="startNewPlan" class="btn btn-primary btn-large" data-testid="new-plan-button">
  <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <path d="M12 5v14M5 12h14"/>
  </svg>
  开始规划新旅程
</button>
```

---

### Task 6: 为聊天页面添加 data-testid

**Files:**
- Modify: `frontend/src/views/ChatView.vue`

- [ ] **Step 1: 为消息列表容器添加 data-testid**

在 `ChatView.vue` 第 78 行，修改消息容器：

```html
<div class="chat-messages" ref="messagesContainer" data-testid="message-list">
```

- [ ] **Step 2: 为 AI 消息添加 data-testid**

在 `ChatView.vue` 第 80-102 行，修改消息渲染部分：

```html
<div
  v-for="(msg, index) in messages"
  :key="index"
  :class="['message', msg.role]"
  :data-testid="msg.role === 'assistant' ? 'assistant-message' : 'user-message'"
>
```

- [ ] **Step 3: 为加载指示器添加 data-testid**

在 `ChatView.vue` 第 105 行，修改加载消息：

```html
<div v-if="loading" class="message assistant loading-message" data-testid="loading-indicator">
```

- [ ] **Step 4: 为消息输入框添加 data-testid**

在 `ChatView.vue` 第 126-133 行，修改输入框：

```html
<textarea
  v-model="inputMessage"
  @keydown.enter.exact.prevent="sendMessage"
  placeholder="描述你的旅行需求..."
  rows="1"
  ref="inputRef"
  class="chat-input"
  data-testid="chat-input"
></textarea>
```

- [ ] **Step 5: 为发送按钮添加 data-testid**

在 `ChatView.vue` 第 134-144 行，修改发送按钮：

```html
<button
  @click="sendMessage"
  :disabled="loading || !inputMessage.trim()"
  class="send-btn"
  title="发送消息"
  data-testid="send-button"
>
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
    <line x1="22" y1="2" x2="11" y2="13"/>
    <polygon points="22 2 15 22 11 13 2 9 22 2"/>
  </svg>
</button>
```

---

### Task 7: 创建认证流程测试

**Files:**
- Create: `frontend/src/e2e/auth.spec.ts`

- [ ] **Step 1: 创建 e2e 目录**

Run: `mkdir -p frontend/src/e2e`
Expected: 目录创建成功

- [ ] **Step 2: 创建 auth.spec.ts 测试文件**

```typescript
import { test, expect } from '@playwright/test';

test.describe('认证流程测试', () => {
  // 生成随机用户数据
  const randomSuffix = Date.now();
  const testUsername = `testuser_${randomSuffix}`;
  const testEmail = `test_${randomSuffix}@example.com`;
  const testPassword = 'password123';

  test('用户注册成功', async ({ page }) => {
    // 访问注册页面
    await page.goto('/register');

    // 填写注册表单
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('email-input').fill(testEmail);
    await page.getByTestId('password-input').fill(testPassword);

    // 点击注册按钮
    await page.getByTestId('register-button').click();

    // 验证跳转到首页
    await expect(page).toHaveURL('/');

    // 验证首页显示新建计划按钮（表示已登录）
    await expect(page.getByTestId('new-plan-button')).toBeVisible();
  });

  test('用户登录成功', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');

    // 填写登录表单
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('password-input').fill(testPassword);

    // 点击登录按钮
    await page.getByTestId('login-button').click();

    // 验证跳转到首页
    await expect(page).toHaveURL('/');

    // 验证首页显示新建计划按钮
    await expect(page.getByTestId('new-plan-button')).toBeVisible();
  });

  test('登录失败显示错误信息', async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');

    // 填写错误的凭据
    await page.getByTestId('username-input').fill('nonexistent_user');
    await page.getByTestId('password-input').fill('wrongpassword');

    // 点击登录按钮
    await page.getByTestId('login-button').click();

    // 验证显示错误信息
    await expect(page.getByTestId('error-message')).toBeVisible();

    // 验证仍在登录页面
    await expect(page).toHaveURL('/login');
  });
});
```

- [ ] **Step 3: 验证测试文件语法**

Run: `cd frontend && npx playwright test --list`
Expected: 显示 3 个测试用例

---

### Task 8: 创建 AI 对话测试

**Files:**
- Create: `frontend/src/e2e/chat.spec.ts`

- [ ] **Step 1: 创建 chat.spec.ts 测试文件**

```typescript
import { test, expect } from '@playwright/test';

test.describe('AI 对话测试', () => {
  const testUsername = `chatuser_${Date.now()}`;
  const testEmail = `chat_${Date.now()}@example.com`;
  const testPassword = 'password123';

  test.beforeAll(async ({ browser }) => {
    // 注册用户
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.goto('/register');
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('email-input').fill(testEmail);
    await page.getByTestId('password-input').fill(testPassword);
    await page.getByTestId('register-button').click();
    await expect(page).toHaveURL('/');

    await context.close();
  });

  test('新建对话并发送消息', async ({ page }) => {
    // 登录
    await page.goto('/login');
    await page.getByTestId('username-input').fill(testUsername);
    await page.getByTestId('password-input').fill(testPassword);
    await page.getByTestId('login-button').click();
    await expect(page).toHaveURL('/');

    // 点击新建计划按钮
    await page.getByTestId('new-plan-button').click();

    // 验证进入聊天页面
    await expect(page).toHaveURL(/\/chat\/\d+/);

    // 发送消息
    const testMessage = '为我计划5月15日，星期五晚从成都出发，前往重庆的两日旅游计划，预算2000元';
    await page.getByTestId('chat-input').fill(testMessage);
    await page.getByTestId('send-button').click();

    // 验证用户消息显示
    const userMessage = page.getByTestId('user-message').last();
    await expect(userMessage).toContainText('成都');
    await expect(userMessage).toContainText('重庆');

    // 等待 AI 响应（最长等待 60 秒）
    await expect(page.getByTestId('loading-indicator')).toBeVisible({ timeout: 5000 });
    await expect(page.getByTestId('assistant-message').last()).toBeVisible({ timeout: 60000 });

    // 验证 AI 响应包含旅行计划相关关键词
    const assistantMessage = page.getByTestId('assistant-message').last();
    const responseText = await assistantMessage.textContent();
    expect(responseText).toBeTruthy();
    expect(responseText!.length).toBeGreaterThan(50);
  });
});
```

- [ ] **Step 2: 验证测试文件语法**

Run: `cd frontend && npx playwright test --list`
Expected: 显示所有测试用例

---

### Task 9: 更新项目文档

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: 在 CLAUDE.md 中添加 E2E 测试说明**

在 `CLAUDE.md` 的"常用命令"部分添加 E2E 测试命令：

```markdown
### 前端 (frontend/)
```bash
npm run dev        # 启动开发服务器
npm run build      # 构建生产版本
npm run test       # 运行测试 (watch 模式)
npm run test:run   # 运行测试 (单次)
npm run test:e2e   # 运行端到端测试 (需先启动后端)
npm run test:e2e:ui # 带 UI 运行端到端测试
```
```

- [ ] **Step 2: 在 CLAUDE.md 中添加测试约定说明**

在"测试约定"部分添加 E2E 测试说明：

```markdown
### E2E 测试约定

- 使用 Playwright 进行端到端测试
- 测试文件位于 `frontend/src/e2e/`
- 关键元素使用 `data-testid` 属性定位
- 运行 E2E 测试前需启动后端服务
```

---

### Task 10: 运行测试验证

**Files:**
- None

- [ ] **Step 1: 确保后端服务运行**

Run: `cd backend && mvn spring-boot:run`
Expected: 后端服务启动成功（端口 8080）

- [ ] **Step 2: 运行 E2E 测试**

Run: `cd frontend && npm run test:e2e`
Expected: 所有测试通过

- [ ] **Step 3: 提交更改**

```bash
git add frontend/package.json frontend/playwright.config.ts frontend/src/e2e/ frontend/src/views/ CLAUDE.md
git commit -m "feat: add Playwright E2E tests for auth and chat flows

- Add Playwright dependency and configuration
- Add data-testid attributes to RegisterView, LoginView, HomeView, ChatView
- Create auth.spec.ts for registration and login tests
- Create chat.spec.ts for AI conversation tests
- Update CLAUDE.md with E2E testing instructions"
```
