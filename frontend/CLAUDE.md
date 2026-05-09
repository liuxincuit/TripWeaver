# CLAUDE.md


## 常用命令

```bash
npm run dev        # 启动开发服务器 (http://localhost:5173)
npm run build      # 构建生产版本
npm run test       # 运行单元测试 (watch 模式)
npm run test:run   # 运行单元测试 (单次)
npm run test:e2e   # 运行 E2E 测试 (需先启动后端)
npm run test:e2e:ui # 带 UI 运行 E2E 测试
```

## 技术栈

- 前端: Vue 3 + Vite 8 + Pinia 3 + Vue Router 5

## 架构要点

### 目录结构

```
src/
├── api/           # API 客户端 (auth.js, chat.js)
├── views/         # 页面组件
├── stores/        # Pinia stores
├── router/        # 路由配置
├── styles/        # 全局样式
├── __tests__/     # 单元测试
└── e2e/           # E2E 测试
```

### API 层

两个独立的 axios 实例 (`auth.js`, `chat.js`)，均配置：
- `baseURL: '/api'`
- 请求拦截器自动注入 JWT token

### 设计系统

全局样式 (`global.css`) 定义了：
- CSS 变量：颜色、间距、圆角、阴影、字体
- 组件类：`.btn`, `.card`, `.input`
- 动画：`fadeInUp`, `float`, `pulse`
- 主题：Explorer Map (温暖的大地色调，复古地图风格)

### 测试约定

- 单元测试：Vitest + Vue Test Utils，文件放在 `src/__tests__/`
- E2E 测试：Playwright，文件放在 `src/e2e/`
- 关键元素使用 `data-testid` 属性定位
- E2E 测试需要后端服务运行中

## 开发代理

Vite 开发服务器代理 `/api` 请求到 `http://localhost:8080`（后端地址）。
