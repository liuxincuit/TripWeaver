# TripWeaver 端到端测试设计

## 1. 概述

为 TripWeaver 项目设计端到端（E2E）测试，验证核心用户流程的正确性。

### 目标

- 自动化验证用户注册、登录、AI 对话等核心流程
- 提供可重复执行的测试脚本，支持 CI/CD 集成
- 通过语义化选择器降低维护成本

### 范围

- 用户注册流程
- 用户登录流程
- AI 对话与旅行计划生成

---

## 2. 技术方案

### 测试框架

**Playwright** - 现代化的端到端测试框架

选择理由：
- 支持多浏览器（Chrome、Firefox、Safari）
- 自动等待机制，减少 flaky 测试
- 强大的选择器和断言能力
- 内置测试隔离（每个测试独立浏览器上下文）
- 支持截图和视频录制，便于调试

### 选择器策略

采用**语义化选择器**（data-testid），为关键元素添加 `data-testid` 属性：

```html
<button data-testid="register-button">注册账户</button>
<input data-testid="username-input" type="text" />
```

优势：
- 与样式解耦，CSS 变化不影响测试
- 与文本解耦，文案修改不影响测试
- 明确标识测试锚点，便于维护

### 测试目录结构

```
frontend/
├── src/
│   ├── __tests__/         # 单元测试（已有）
│   │   └── *.spec.js
│   └── e2e/               # 端到端测试
│       ├── auth.spec.ts   # 认证流程测试
│       └── chat.spec.ts   # AI 对话测试
├── playwright.config.ts   # Playwright 配置
└── package.json           # 添加 Playwright 依赖
```

---

## 3. 测试用例设计

### 3.1 认证流程测试 (auth.spec.ts)

#### 测试用例 1：用户注册

**前置条件：** 无

**步骤：**
1. 访问 `/register` 页面
2. 填写用户名（随机生成，避免重复）
3. 填写邮箱（随机生成）
4. 填写密码
5. 点击注册按钮
6. 验证跳转到首页

**验证点：**
- URL 变为 `/`
- 首页显示用户相关内容

#### 测试用例 2：用户登录

**前置条件：** 已注册用户

**步骤：**
1. 访问 `/login` 页面
2. 填写用户名
3. 填写密码
4. 点击登录按钮
5. 验证跳转到首页

**验证点：**
- URL 变为 `/`
- 首页显示用户相关内容

#### 测试用例 3：登录失败

**前置条件：** 无

**步骤：**
1. 访问 `/login` 页面
2. 填写不存在的用户名
3. 填写错误密码
4. 点击登录按钮

**验证点：**
- 显示错误提示信息
- 仍在登录页面

### 3.2 AI 对话测试 (chat.spec.ts)

#### 测试用例 1：新建对话并发送消息

**前置条件：** 已登录用户

**步骤：**
1. 从首页点击"新建计划"按钮
2. 进入聊天页面
3. 发送消息："为我计划5月15日，星期五晚从成都出发，前往重庆的两日旅游计划，预算2000元"
4. 等待 AI 响应

**验证点：**
- 消息显示在对话区域
- AI 返回非空响应
- 响应包含旅行计划相关关键词（如"行程"、"住宿"、"交通"等）

#### 测试用例 2：对话历史加载

**前置条件：** 已有对话历史

**步骤：**
1. 登录用户
2. 访问已有计划的聊天页面

**验证点：**
- 历史消息正确加载
- 消息顺序正确

---

## 4. 需要添加的 data-testid

### 注册页面 (RegisterView.vue)

| 元素 | data-testid |
|------|-------------|
| 用户名输入框 | `username-input` |
| 邮箱输入框 | `email-input` |
| 密码输入框 | `password-input` |
| 注册按钮 | `register-button` |
| 错误提示 | `error-message` |
| 登录链接 | `login-link` |

### 登录页面 (LoginView.vue)

| 元素 | data-testid |
|------|-------------|
| 用户名输入框 | `username-input` |
| 密码输入框 | `password-input` |
| 登录按钮 | `login-button` |
| 错误提示 | `error-message` |
| 注册链接 | `register-link` |

### 首页 (HomeView.vue)

| 元素 | data-testid |
|------|-------------|
| 新建计划按钮 | `new-plan-button` |

### 聊天页面 (ChatView.vue)

| 元素 | data-testid |
|------|-------------|
| 消息输入框 | `chat-input` |
| 发送按钮 | `send-button` |
| 消息列表 | `message-list` |
| AI 消息 | `assistant-message` |
| 用户消息 | `user-message` |
| 加载指示器 | `loading-indicator` |

---

## 5. 测试执行

### 本地执行

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次）
npm install

# 运行测试
npm run test:e2e

# 带 UI 运行测试
npm run test:e2e:ui
```

### CI/CD 集成

测试脚本可集成到 CI/CD 流程：

```yaml
# 示例 GitHub Actions 配置
- name: Run E2E tests
  run: |
    cd frontend
    npm run test:e2e
```

### 测试前置条件

运行测试前需要：
1. 启动后端服务（`mvn spring-boot:run`）
2. 启动前端服务（`npm run dev`）
3. 确保 AI API 配置正确

---

## 6. 实施计划

1. **添加 Playwright 依赖** - 更新 frontend/package.json
2. **创建 Playwright 配置** - 创建 playwright.config.ts
3. **添加 data-testid** - 更新前端组件
4. **编写测试脚本** - 创建 auth.spec.ts 和 chat.spec.ts
5. **验证测试** - 运行测试确保通过

---

## 7. 维护指南

### 页面变更时

1. 如果元素结构变化，检查对应的 data-testid 是否需要更新
2. 如果新增功能，添加对应的测试用例
3. 如果删除功能，删除对应的测试用例

### 选择器更新

当 data-testid 变化时，只需更新测试文件中的选择器：

```typescript
// 旧
page.getByTestId('register-button')

// 新（如果 data-testid 改为 'submit-button'）
page.getByTestId('submit-button')
```
