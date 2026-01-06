# 任务板（Task Board）

## 状态说明
- `DONE`：已完成
- `IN_PROGRESS`：进行中
- `TODO`：待办
- `BLOCKED`：阻塞（需先更新 SSOT 或等待依赖）

## 当前里程碑
### 协同文档（SSOT）
- DONE: `00_project_rules.md` 初始规范
- DONE: `01_database_schema.md` 数据库设计（含 DDL）
- DONE: `02_api_spec.md` API 契约
- DONE: `03_constants.md` 共同语言（枚举/节次）

### 数据准备
- DONE: `data/seed_data.sql` 种子数据（>=20 courses, >=5 constraints，含冲突场景）

## 前端（/frontend）
- DONE: 初始化完成（Vue 3 + Vite 环境搭建）
- TODO: 约束编辑页面/组件（对齐 `02_api_spec.md` 的 `POST /api/constraints`）
- TODO: 触发排课页面/流程（`POST /api/schedule/generate`）
- TODO: 结果展示页面（`GET /api/schedule/result`，支持状态展示与轮询）

## 后端（/backend）
- IN_PROGRESS: 初始化进行中（Spring Boot 3 + Maven + PostgreSQL + Timefold）
- TODO: 数据库迁移（Flyway/Liquibase），落地 `01_database_schema.md` DDL
- TODO: 加载种子数据（可选：dev profile 自动导入 `data/seed_data.sql`）
- TODO: 实现 API：
  - `POST /api/constraints`
  - `POST /api/schedule/generate`
  - `GET /api/schedule/result`
- TODO: Timefold 求解模型与约束实现（对齐 `03_constants.md` 的 constraintKey/params）

## 阻塞项（如有）
- 无
