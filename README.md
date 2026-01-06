# Smart Course Scheduler

[![Version](https://img.shields.io/badge/version-v1.0-blue)](#)
[![Status](https://img.shields.io/badge/status-MVP-success)](#)

A **Timefold-powered smart course scheduling system**.

Smart Course Scheduler takes a set of courses + user constraints, then generates a feasible timetable by optimizing **Hard/Soft constraints** via **Timefold Solver**.

---

## Dashboard (Screenshot)

Screenshot placeholder:

- Add your image at `docs/assets/dashboard.png`
- Recommended: 1440x900 PNG

---

## Core Features

- **End-to-end full stack**
  - Backend: Spring Boot 3 (REST API)
  - Database: PostgreSQL + Flyway migrations
  - Solver: Timefold (Hard/Soft Score)
  - Frontend: Vue 3 + Element Plus
  - Smoke test: Python script
- **Asynchronous scheduling workflow**
  - Trigger solve (POST) -> Poll status (GET) -> Fetch result (GET)
- **Constraint-driven scheduling**
  - V1.0 validates `NO_MORNING_CLASSES` (pushes courses to afternoon/evening)

---

## Quick Start

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+ (or newer)
- PostgreSQL 15+ (recommended)

### Backend (Spring Boot)

1) Configure your local PostgreSQL

The default configuration is in `backend/src/main/resources/application.yml`:

- DB: `smart_course_scheduler`
- Username: `postgres`
- Password: `password`

2) Run backend

```bash
cd backend
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`.

### Frontend (Vue 3)

```bash
cd frontend
npm install
npm run dev
```

---

## API Contract & Manual Verification

The single source of truth (SSOT) is under `/_team_sync`:

- API Spec: `_team_sync/02_api_spec.md`
- curl commands: `_team_sync/06_curl_examples.md`
- Python smoke test: `_team_sync/tests/test_scheduler_api.py`

---

## Repository Layout

- `backend/` Spring Boot + Timefold + Flyway
- `frontend/` Vue 3 + Element Plus
- `_team_sync/` SSOT docs (schema, API contract, constants, verification scripts)

---

## Milestone Report

- V1.0 release snapshot: `_team_sync/07_milestone_v1.0.md`
