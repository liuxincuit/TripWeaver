# Batch 2: Status Field + Exceptions + Axios + @Transactional

> **For agentic workers:** Use superpowers:subagent-driven-development to implement.

**Goal:** 补全数据模型，统一错误处理，消除前端重复代码，保障数据一致性。

**Tech Stack:** Spring Boot 3.5.14, Vue 3, Pinia 3

---

## File Structure

| 文件 | 职责 | 改动类型 |
|------|------|----------|
| `backend/src/main/java/com/tripweaver/entity/TravelPlan.java` | 添加 status 字段 | 修改 |
| `backend/src/main/java/com/tripweaver/exception/BusinessException.java` | 业务异常基类 | 新增 |
| `backend/src/main/java/com/tripweaver/exception/UserAlreadyExistsException.java` | 409 用户冲突 | 新增 |
| `backend/src/main/java/com/tripweaver/exception/AuthenticationException.java` | 401 认证失败 | 新增 |
| `backend/src/main/java/com/tripweaver/config/GlobalExceptionHandler.java` | 全局异常处理 | 新增 |
| `backend/src/main/java/com/tripweaver/service/UserService.java` | 替换 RuntimeException | 修改 |
| `frontend/src/api/client.js` | 统一 axios 实例 | 新增 |
| `frontend/src/api/auth.js` | 改用 client.js | 修改 |
| `frontend/src/api/chat.js` | 改用 client.js | 修改 |

---

### Task 1: TravelPlan 添加 status 字段

**Files:** Modify `backend/src/main/java/com/tripweaver/entity/TravelPlan.java`

在 `content` 字段前添加：

```java
private String status = "draft"; // draft, planning, confirmed, completed
```

`mvn test` 验证，提交。

---

### Task 2: 自定义异常 + 全局异常处理器

**Files:**
- Create: `backend/src/main/java/com/tripweaver/exception/BusinessException.java`
- Create: `backend/src/main/java/com/tripweaver/exception/UserAlreadyExistsException.java`
- Create: `backend/src/main/java/com/tripweaver/exception/AuthenticationException.java`
- Create: `backend/src/main/java/com/tripweaver/config/GlobalExceptionHandler.java`
- Modify: `backend/src/main/java/com/tripweaver/service/UserService.java`

**异常层次：**
```
BusinessException(code, message)
├── UserAlreadyExistsException(code=409)
└── AuthenticationException(code=401)
```

**GlobalExceptionHandler:** `@RestControllerAdvice`，处理 `BusinessException`（返回对应 code）和 `Exception`（返回 500）。

**UserService:** 将 `throw new RuntimeException("用户名已存在")` 替换为 `throw new UserAlreadyExistsException("用户名已存在")`，同理替换"邮箱已被注册"。

`mvn test` + E2E 验证，提交。

---

### Task 3: 前端 axios 实例统一

**Files:**
- Create: `frontend/src/api/client.js`
- Modify: `frontend/src/api/auth.js`
- Modify: `frontend/src/api/chat.js`

**client.js:** 导出带 baseURL `/api`、Content-Type header、JWT 拦截器的 axios 实例。

**auth.js 和 chat.js:** 删除各自的 axios 创建代码，改为 `import api from './client'`。

E2E 验证，提交。

---

### Task 4: createNewPlan 添加 @Transactional

**Files:** Modify `backend/src/main/java/com/tripweaver/service/ChatService.java`

在 `createNewPlan()` 方法上加 `@Transactional`：

```java
import org.springframework.transaction.annotation.Transactional;

@Transactional
public Long createNewPlan() {
```

`mvn test` 验证，提交。
