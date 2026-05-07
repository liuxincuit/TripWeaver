# TripWeaver 功能设计文档

## 1. 项目概述

**TripWeaver** 是一个基于人工智能的旅行规划系统。用户通过自然语言对话描述旅行需求，系统生成完整的旅行计划。

### 目标用户

个人旅行者（为自己或家人规划旅行）

### 核心价值

- 自然语言交互，降低使用门槛
- AI 智能生成，提供个性化方案
- 一站式覆盖旅行全要素

---

## 2. 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21 + Spring Boot 3.x + Spring AI |
| 前端 | Vue 3 + Vite |
| 数据库 | H2 |
| AI 模型 | 可配置（OpenAI/Claude/国内模型） |

---

## 3. 项目结构

```
TripWeaver/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   ├── controller/         # REST API
│   │   ├── service/            # 业务逻辑
│   │   ├── ai/                 # Spring AI 集成
│   │   ├── tools/              # 外部 API 工具
│   │   ├── entity/             # 数据实体
│   │   ├── repository/         # 数据访问
│   │   └── config/             # 配置类
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 通用组件
│   │   ├── api/                # API 调用
│   │   └── stores/             # 状态管理
│   └── package.json
└── docs/                       # 文档
```

---

## 4. 功能模块

### 4.1 用户模块

- 用户注册/登录
- 会话管理（JWT Token）
- 历史计划查看与管理

### 4.2 AI 对话模块

- 自然语言理解用户需求
- 多轮对话澄清需求细节
- 生成结构化旅行计划
- 流式响应（实时显示生成内容）

### 4.3 工具调用模块

按需逐步接入外部 API：

| 工具 | 用途 | 优先级 |
|------|------|--------|
| 天气查询 API | 获取目的地天气预报 | 高 |
| 交通查询 API | 查询航班、高铁、大巴等 | 高 |
| 酒店查询 API | 搜索酒店信息与价格 | 中 |
| 景点查询 API | 获取景点介绍与门票 | 中 |
| 美食推荐 API | 推荐当地特色美食 | 低 |

### 4.4 计划生成模块

生成旅行计划的 11 个要点：

**核心要点：**
1. 行程 - 每日行程安排
2. 住宿 - 酒店推荐与预订建议
3. 交通 - 往返及当地交通方案
4. 美食 - 餐厅推荐与特色美食
5. 预算 - 费用估算与分配建议
6. 天气 - 目的地天气预报

**可选要点：**
7. 证件 - 身份证、护照、签证等提醒
8. 安全 - 目的地安全注意事项
9. 健康 - 医疗准备与健康建议
10. 行李 - 行李清单建议
11. 通讯 - 电话卡、网络等建议

---

## 5. 数据模型

### User（用户）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| username | String | 用户名（唯一） |
| password | String | 密码（加密存储） |
| email | String | 邮箱（唯一） |
| createdAt | LocalDateTime | 创建时间 |

### TravelPlan（旅行计划）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| userId | Long | 用户 ID |
| title | String | 计划标题 |
| destination | String | 目的地 |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |
| content | JSON | 完整计划内容（结构化） |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### Conversation（对话会话）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| userId | Long | 用户 ID |
| planId | Long | 关联计划 ID |
| messages | JSON | 对话历史 |
| createdAt | LocalDateTime | 创建时间 |

---

## 6. API 设计

### 认证相关

```
POST /api/auth/register     # 用户注册
POST /api/auth/login        # 用户登录
POST /api/auth/logout       # 用户登出
GET  /api/auth/me           # 获取当前用户信息
```

### 旅行计划

```
GET    /api/plans           # 获取用户计划列表
GET    /api/plans/{id}      # 获取计划详情
DELETE /api/plans/{id}      # 删除计划
```

### AI 对话

```
POST /api/chat/send         # 发送消息（流式响应）
GET  /api/chat/history/{planId}  # 获取对话历史
POST /api/chat/new          # 创建新对话
```

---

## 7. 前端页面

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | /login | 用户登录 |
| 注册页 | /register | 用户注册 |
| 首页 | / | 新建计划入口，展示最近计划 |
| 对话页 | /chat/{planId} | 核心交互页面 |
| 历史列表页 | /plans | 用户所有计划列表 |
| 计划详情页 | /plan/{id} | 查看已生成的计划 |

---

## 8. 开发阶段规划

### Phase 1：基础框架
- 项目初始化（前后端）
- 用户认证功能
- 基础页面框架

### Phase 2：AI 对话核心
- Spring AI 集成
- 多模型配置
- 对话功能实现
- 流式响应

### Phase 3：计划生成
- 提示词工程
- 结构化输出生成
- 11 个要点全覆盖

### Phase 4：工具集成
- 工具调用框架
- 天气 API 接入
- 其他 API 按需接入

### Phase 5：优化完善
- 用户体验优化
- 性能优化
- 测试覆盖

---

## 9. 部署方案

MVP 阶段：
- 单机部署
- H2 文件数据库
- 内置 AI API Key 配置

后期扩展：
- 容器化部署
- 迁移至生产级数据库
- 多实例负载均衡
