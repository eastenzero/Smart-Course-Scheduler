# Smart Course Scheduler — V1.0 MVP 发布归档报告（SSOT 快照）

版本：`v1.0`  
状态：`MVP 已上线`  
归档位置：`/_team_sync/07_milestone_v1.0.md`  

本报告记录 Smart Course Scheduler 达成 **V1.0 MVP** 的最终状态快照：

- 这是一份对外可展示的发布报告
- 这是一份对内可复用的战役复盘资产
- 这是一份对未来迭代可追溯的 SSOT（Single Source of Truth）

---

## Executive Summary

V1.0 交付的不是“一个能跑的 Demo”，而是一条可被重复验收、可被持续演进的全链路系统。

- **全栈打通**：Spring Boot（API）× PostgreSQL/Flyway（数据）× Timefold（求解）× Vue 3/Element Plus（界面）× Python（自动化）形成闭环。
- **算法落地**：Timefold 以 Hard/Soft Score 驱动排课优化，产出可解释的结果与评分。
- **核心业务验证**：`NO_MORNING_CLASSES` 约束可控、可验收；启用后课程被系统性推到下午/晚上。
- **异步任务架构跑通**：触发求解 -> 状态轮询 -> 结果获取，形成标准 Job 模式。
- **关键链路 100% 覆盖（面向 MVP 验收）**：通过脚本/命令覆盖 `courses -> constraints -> generate -> status/result` 的关键路径，实现“后端说 OK 后 1 分钟内可复测”。

---

## Battle Report

这一章记录我们的“至暗时刻”与对应的工程化修复。它们不是事故，而是团队资产：每一次踩坑，都把系统的边界与规则变得更清晰。

### 1) Flyway 指纹危机（Checksum 不匹配）

- **现象**：应用启动失败，Flyway 报 checksum mismatch。
- **根因**：手动修改了已执行过的迁移脚本（典型为 V2），破坏迁移不可变性。
- **处置**：
  - 在开发环境重置 schema，恢复一致性。
  - 将后续修正通过新增迁移（例如 V3）完成，避免“改历史”。
- **沉淀原则**：
  - **迁移脚本一旦执行即不可变**。
  - 变更只能通过新增版本化迁移表达，确保可追溯、可回放。

### 2) 0 分白卷事件（求解器跑通但结果为空）

- **现象**：求解器完成但排课条目为空，Score 看似“完美”（0/0）。
- **根因**：规划变量允许 `null`，Timefold 找到“全部不排”的平凡解，且不会触发硬约束惩罚。
- **修复**：
  - 强制规划变量非空。
  - 将“必须排课”约束提升为硬约束，逼迫求解器输出真实课表。
- **沉淀原则**：
  - 规划模型必须明确“可行解”的定义；否则求解器会用最省力的方式赢。

### 3) 前端 400 Bad Request（轮询链路断裂）

- **现象**：轮询状态接口返回 400，后端日志显示 DTO 校验失败。
- **根因**：前端调用链未透传 `scheduleId`（异步任务的唯一关联 ID）。
- **修复**：
  - 修复前端 API 调用链路，确保 `scheduleId` 从触发到轮询贯穿全链路。
  - 在 SSOT 中固化契约（必填参数、错误格式、状态枚举）。
- **沉淀原则**：
  - 异步系统的“相关性 ID”不是可选项，是架构主干；必须在 API 契约层明确。

### 4) SSOT 分裂（枚举不一致引发 500）

- **现象**：数据库使用 `NO_EARLY_CLASS`，代码/文档使用 `NO_MORNING_CLASSES`，出现逻辑异常甚至 500。
- **根因**：概念在多个地方被重复定义且漂移，导致同一枚举值出现多个 key。
- **修复**：
  - 增加 V3 数据清洗迁移，将旧 key 标准化为新标准。
  - 以 `_team_sync` 为 SSOT，同步 schema、常量、API 文档、种子数据与测试脚本。

---

## Tech Stack

| Layer | Tech | Notes |
| --- | --- | --- |
| Backend | Spring Boot 3.2.2, Java 21 | REST API + Validation + JPA |
| Solver | Timefold Solver 1.29.0 | Hard/Soft Score 排课优化 |
| Database | PostgreSQL（建议 15+） | 课程、约束、任务结果持久化 |
| Migration | Flyway 10.10.0 | V1 schema / V2 seed / V3 标准化 |
| Frontend | Vue 3, Element Plus, Vite | MVP UI + 异步任务轮询 |
| Testing | Python 3 + requests | 冒烟测试关键链路 |

---

## Roadmap

### v1.1（短期）：从“能排”到“可控可调”

- Pinning：允许用户锁定部分课程/时间槽（手动调整后保持稳定）
- 约束增强：教师冲突、教室容量、同课多班并行等

### v1.2（中期）：从“单机联调”到“多用户系统”

- Spring Security 用户认证
- 多用户数据隔离、历史任务回溯与可观测性增强

### v2.0（长期）：产品化与生态对接

- Excel/CSV 导入
- iCal 导出
- 面向真实使用场景的“配置化约束”与“可解释性报告”

---

## 附录：V1.0 SSOT 与验收入口

- SSOT 规则：`/_team_sync/00_project_rules.md`
- DB Schema：`/_team_sync/01_database_schema.md`
- API 契约：`/_team_sync/02_api_spec.md`
- 常量枚举：`/_team_sync/03_constants.md`
- curl 验收：`/_team_sync/06_curl_examples.md`
- 自动化冒烟：`/_team_sync/tests/test_scheduler_api.py`
