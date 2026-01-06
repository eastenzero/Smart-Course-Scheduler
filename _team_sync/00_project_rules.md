# Smart Course Scheduler - 项目规范书（SSOT）

## 1. 项目目标
Smart Course Scheduler（多功能智能课程表）提供“课程数据 + 用户偏好（约束）+ 自动排课（Timefold）”的一体化能力。

- 输入：课程列表（含可选时间段/固定时间段）、用户偏好与排课规则（含权重）。
- 处理：后端使用 Timefold 进行约束求解与评分。
- 输出：可视化课程表结果与评分/解释信息。

## 2. 技术栈与版本
### 2.1 后端
- Java：17 或 21（推荐 21）
- Spring Boot：3.x（推荐 3.2+ / 3.3+）
- 构建工具：Maven 3.8+（推荐 3.9+）
- 数据库：PostgreSQL 15+
- 约束求解：Timefold（与 Spring Boot 3 兼容版本）

### 2.2 前端
- Node.js：18+
- 框架：Vue 3
- 构建：Vite

## 3. 仓库目录规范
仓库根目录下：

- `/backend`
  - Spring Boot 代码、Timefold 求解逻辑、数据库访问层、REST API
- `/frontend`
  - Vue3 + Vite 前端工程
- `/_team_sync`
  - 项目协同的唯一真理来源（Single Source of Truth, SSOT）
  - `schema.sql` / `api_spec.md` / 其他约定文档

## 4. 协同与变更流程（强约束）
- `/_team_sync` 为 SSOT：前后端实现必须严格遵循该目录下文档。
- 任何接口或数据结构调整必须走“文档变更流程”：
  - 发现问题 -> 提交变更建议 -> 由（文档负责人）更新 `/_team_sync` -> 前后端再同步修改代码。

## 5. API 约定（全局）
- Base Path：`/api`
- 数据格式：`application/json; charset=utf-8`
- 时间格式：ISO-8601 `timestamptz`（示例：`2026-01-05T17:39:00Z`）
- 统一错误响应：

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "...",
    "details": [{"field": "xxx", "reason": "..."}]
  }
}
```

## 6. Git 提交规范
采用 Conventional Commits（精简版）：

- `feat: ...` 新功能
- `fix: ...` 修复
- `refactor: ...` 重构（无功能变化）
- `docs: ...` 文档
- `test: ...` 测试
- `chore: ...` 杂项/维护
- `build: ...` 构建系统或依赖
- `ci: ...` CI 配置

建议分支命名：
- `feat/<short-name>`
- `fix/<short-name>`
- `chore/<short-name>`

## 7. 配置与环境变量（建议）
后端（示例）：
- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/smart_scheduler`
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=...`
- `APP_TIMEZONE=UTC`（可选）

前端（示例）：
- `VITE_API_BASE_URL=http://localhost:8080`（可选，默认同源）

## 8. 质量门槛（建议）
- 后端：REST 接口具备参数校验与清晰错误码；Timefold 求态可追踪。