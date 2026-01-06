# Next Steps（后端：Flyway 接入指引）

## 1. Maven 依赖（推荐）
在 `backend/pom.xml` 增加（版本通常由 Spring Boot BOM 管理，无需显式写 version）：

- **Flyway 核心**
  - `groupId`: `org.flywaydb`
  - `artifactId`: `flyway-core`

- **Flyway PostgreSQL 支持（如你使用的 Flyway 版本要求拆分数据库模块时需要）**
  - `groupId`: `org.flywaydb`
  - `artifactId`: `flyway-database-postgresql`

- **PostgreSQL JDBC 驱动**
  - `groupId`: `org.postgresql`
  - `artifactId`: `postgresql`

> 迁移文件位置已固定为：`classpath:db/migration`，当前已生成：
> - `V1__init_schema.sql`
> - `V2__seed_data.sql`

## 2. application.yml 最小配置（示例）
在 `backend/src/main/resources/application.yml` 中加入（按实际账号/库名修改）：

```yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_scheduler
    username: smart_scheduler
    password: smart_scheduler
  flyway:
    enabled: true
    locations: classpath:db/migration
```

## 3. 常见建议（可选）
- 若本地开发经常重置数据库，可在开发环境 profile 中使用：

```yml
spring:
  flyway:
    clean-disabled: false
```

- 若未来需要区分 schema（如 `public` 之外），可补充：

```yml
spring:
  flyway:
    schemas: public
```
