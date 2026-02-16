# Smart Course Scheduler

[![Version](https://img.shields.io/badge/version-v1.0-blue)](#)
[![Status](https://img.shields.io/badge/status-MVP-success)](#)

A **Timefold-powered smart course scheduling system** — takes a set of courses + user constraints, then generates a feasible timetable by optimizing **Hard/Soft constraints** via **Timefold Solver**.

---

## Functional Preview

![Functional Preview](screenshots/preview.webp)

---

## Environment Requirements

| Dependency | Version  | Notes |
|------------|----------|-------|
| JDK        | 17+      | Required |
| Maven      | 3.8+     | Required |
| Node.js    | 18+ LTS  | 18 or 20 LTS recommended |
| PostgreSQL | 14+      | Only for production mode; H2 mode needs no DB |

---

## Quick Start — H2 Mode (Zero Install)

No database installation required. Use this for quick verification.

### 1) Start Backend (H2)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev-h2
```
- Backend: `http://localhost:18081`
- H2 Console: `http://localhost:18081/h2-console`

### 2) Start Frontend
```bash
cd frontend
npm install   # first time only
npm run dev -- --port 15173 --host
```
- Frontend: `http://localhost:15173`

---

## Quick Start — PostgreSQL Mode (Production)

### 1) Prepare Database

Default config in `backend/src/main/resources/application.yml`:

| Item     | Default Value |
|----------|---------------|
| Host     | `localhost:5432` |
| Database | `smart_course_scheduler` |
| Username | `postgres` |
| Password | `password` |

```sql
CREATE DATABASE smart_course_scheduler;
```

### 2) Start Backend
```bash
cd backend
mvn spring-boot:run
```
- Backend: `http://localhost:8080`
- Flyway auto-creates tables + seed data on first start.

### 3) Start Frontend
```bash
cd frontend
npm install   # first time only
npm run dev -- --port 15173 --host
```

---

## Port Summary

| Service  | H2 (dev-h2) | PostgreSQL (default) |
|----------|-------------|---------------------|
| Backend  | 18081       | 8080                |
| Frontend | 15173       | 15173               |

---

## Database Mode

| Profile    | DB         | Config File |
|------------|------------|-------------|
| *(default)* | PostgreSQL | `application.yml` |
| `dev-h2`   | H2 (in-memory, MODE=PostgreSQL) | `application-dev-h2.yml` |

H2 mode uses `MODE=PostgreSQL` for SQL compatibility. Data resets on each restart.

---

## Test Account

This project is a **scheduling API** — it does not have user login/accounts.
- Seed data (25 courses + 5 constraints) is loaded via `V2__seed_data.sql`.
- Interact via REST API or Frontend UI directly.

---

## Core Features

- **End-to-end full stack**: Spring Boot 3 REST API + Vue 3 + Element Plus
- **Solver**: Timefold (Hard/Soft Score)
- **Database**: PostgreSQL + Flyway migrations (H2 for dev)
- **Asynchronous scheduling workflow**: Trigger (POST) → Poll (GET) → Fetch result (GET)
- **Constraint-driven**: V1.0 validates `NO_MORNING_CLASSES`, `NO_FRIDAY`, `PREFER_COMPACT_DAYS`, etc.
- **Smoke test**: Python script in `_team_sync/tests/`

---

## API Contract & Manual Verification

The single source of truth (SSOT) is under `_team_sync/`:

| Document | Path |
|----------|------|
| API Spec | `_team_sync/02_api_spec.md` |
| curl Examples | `_team_sync/06_curl_examples.md` |
| Python Smoke Test | `_team_sync/tests/test_scheduler_api.py` |

---

## Repository Layout

```
Smart-Course-Scheduler/
├── backend/          # Spring Boot + Timefold + Flyway
├── frontend/         # Vue 3 + Element Plus
├── _team_sync/       # SSOT docs (schema, API, constants, verification)
├── screenshots/      # UI preview images
└── README.md
```

---

## FAQ

**Q: H2 mode 启动后数据是否持久化？**
A: 不会。H2 使用内存模式 (`mem:scheduler`)，每次重启数据会重置。Flyway + seed SQL 会自动重新加载。

**Q: 如何添加自定义约束？**
A: 在 `constraints` 表中 INSERT 新记录，`constraint_key` 需在 Solver 代码中有对应实现。详见 `_team_sync/03_constants.md`。

**Q: 前端代理如何配置？**
A: `frontend/vite.config.ts` 中 `/api` 代理到后端地址，H2 模式下需确认端口匹配 (18081)。

---

## Milestone Report

- V1.0 release snapshot: `_team_sync/07_milestone_v1.0.md`
