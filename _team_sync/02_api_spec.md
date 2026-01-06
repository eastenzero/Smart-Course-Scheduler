# API 契约（SSOT）

Base Path：`/api`

## 1. 通用约定
### 1.1 Content-Type
- 请求/响应：`application/json; charset=utf-8`

### 1.2 时间
- 所有时间字段使用 ISO-8601（UTC `Z` 或带时区偏移）

### 1.3 统一错误格式

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Human readable message",
    "details": [
      { "field": "constraints[0].weight", "reason": "must be integer" }
    ]
  }
}
```

### 1.4 状态枚举
- `SolverJobStatus`：`SOLVING | SOLVED | ERROR`

状态映射（建议实现）：
- `SOLVING` -> 数据库存储状态 `QUEUED` / `RUNNING`
- `SOLVED` -> 数据库存储状态 `COMPLETED`
- `ERROR` -> 数据库存储状态 `FAILED`

## 2. 数据模型（JSON）
### 2.1 Constraint

```json
{
  "id": "uuid",
  "constraintKey": "NO_MORNING_CLASSES",
  "weight": 10,
  "enabled": true,
  "params": { "morningEnd": "12:00" }
}
```

### 2.2 Schedule（概览）

```json
{
  "id": "uuid",
  "status": "SOLVED",
  "requestedAt": "2026-01-05T17:39:00Z",
  "startedAt": "2026-01-05T17:39:05Z",
  "finishedAt": "2026-01-05T17:39:10Z",
  "score": { "hard": 0, "soft": -12 },
  "result": {
    "entries": [
      {
        "courseId": "uuid",
        "courseName": "Math",
        "dayOfWeek": 1,
        "start": "08:00",
        "end": "09:40",
        "location": "Room 101",
        "teacher": "Alice"
      }
    ]
  },
  "errorMessage": null
}
```

### 2.3 Course

```json
{
  "id": "uuid",
  "name": "Calculus I",
  "teacher": "Dr. Li",
  "location": "Science Bldg 201",
  "timeSlots": [
    {
      "dayOfWeek": 1,
      "start": "08:00",
      "end": "09:40",
      "weeks": [1,2,3],
      "type": "FIXED"
    }
  ]
}
```

## 3. 接口定义

### 3.1 获取课程列表
`GET /api/courses`

用途：获取当前数据库中的课程列表，供测试脚本与前端选择参与排课的课程。

Response 200：

```json
{
  "courses": [
    {
      "id": "uuid",
      "name": "Calculus I",
      "teacher": "Dr. Li",
      "location": "Science Bldg 201",
      "timeSlots": []
    }
  ]
}
```

可能错误：
- 500 `INTERNAL_ERROR`

### 3.2 保存用户偏好（约束）
`POST /api/constraints`

用途：保存（覆盖式）当前约束集合。后端可选择“全量覆盖”策略：以请求体为准重写 `constraints` 表。

Request Body：

```json
{
  "constraints": [
    {
      "constraintKey": "NO_MORNING_CLASSES",
      "weight": 10,
      "enabled": true,
      "params": { "morningEnd": "12:00" }
    },
    {
      "constraintKey": "NO_FRIDAY",
      "weight": 5,
      "enabled": true,
      "params": { "bannedDayOfWeek": 5 }
    }
  ]
}
```

Response 200：

```json
{
  "constraints": [
    {
      "id": "uuid",
      "constraintKey": "NO_MORNING_CLASSES",
      "weight": 10,
      "enabled": true,
      "params": { "morningEnd": "12:00" }
    }
  ]
}
```

可能错误：
- 400 `VALIDATION_ERROR`

### 3.3 触发排课任务（异步）
`POST /api/schedule/generate`

用途：触发一次 Timefold 求解。后端必须返回 `scheduleId`（JobID），前端/脚本通过 `GET /api/schedule/status` 轮询状态，最终通过 `GET /api/schedule/result` 获取结果。

Request Body（最小）：

```json
{
  "courseIds": ["uuid", "uuid"],
  "options": {
    "maxSeconds": 10
  }
}
```

字段说明：
- `courseIds`：本次参与排课的课程 ID 列表
- `options.maxSeconds`：求解时间上限（秒，可选）

Response 202：

```json
{
  "scheduleId": "uuid",
  "status": "SOLVING"
}
```

可能错误：
- 400 `VALIDATION_ERROR`
- 409 `CONFLICT`（例如已有 RUNNING 的任务且系统限制并发）

### 3.4 查询排课任务状态（轮询）
`GET /api/schedule/status`

Query Params：
- `scheduleId`（必填）：任务 ID

Response 200：

```json
{
  "scheduleId": "uuid",
  "status": "SOLVING"
}
```

状态枚举：`SOLVING | SOLVED | ERROR`

可能错误：
- 404 `NOT_FOUND`

### 3.5 获取排课结果
`GET /api/schedule/result`

Query Params：
- `scheduleId`（可选）：指定任务 ID；若不传，默认返回“最新一次任务”。

Response 200：

```json
{
  "schedule": {
    "id": "uuid",
    "status": "SOLVING",
    "requestedAt": "2026-01-05T17:39:00Z",
    "startedAt": "2026-01-05T17:39:05Z",
    "finishedAt": null,
    "score": {},
    "result": {},
    "errorMessage": null
  }
}
```

当 `status=SOLVED` 时，`result.entries` 必须返回完整课程表。

可能错误：
- 404 `NOT_FOUND`（指定 scheduleId 不存在）

## 4. 兼容性说明
- `constraintKey` 为受控枚举值：前端应从后端提供的固定列表（未来可新增 `GET /api/constraints/keys`）渲染选择器；在该接口未提供前，前端临时内置与后端一致的枚举集合。
