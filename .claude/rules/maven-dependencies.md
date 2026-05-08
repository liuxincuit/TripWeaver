---
paths:
  - "backend/pom.xml"
---

# Maven 依赖管理规则

## 版本管理原则

所有依赖版本统一由 Spring Boot Parent POM 管理，**禁止显式定义版本号**，除非：

1. 该依赖不在 Spring Boot 的依赖管理中
2. 有明确的业务需求需要使用特定版本

不在 Spring Boot 管理中的依赖，版本号**必须统一定义在 `<properties>` 中**。

## 依赖添加流程

1. 先检查依赖是否由 Spring Boot 管理：查看 [Spring Boot Dependencies BOM](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/)
2. 如果由 Spring Boot 管理，**不添加 version 标签**
3. 如果不在 Spring Boot 管理中，添加版本号并注明原因

## 修改后验证

修改 `pom.xml` 后**必须**执行以下命令确保所有单元测试通过：

```bash
cd backend && mvn test
```