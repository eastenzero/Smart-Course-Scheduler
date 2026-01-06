# 数据库设计（PostgreSQL 15+）

## 1. 设计目标
- 支持课程数据存储（包括“灵活时间段”描述）。
- 支持用户约束（偏好/规则）存储，便于扩展（JSONB 参数）。
- 支持排课结果存储（包含求解状态、得分、结果快照）。

## 2. JSONB 约定
### 2.1 `courses.time_slots`（JSONB）
用于描述课程可选/固定时间段。建议结构（数组）：

```json
[
  {
    "dayOfWeek": 1,
    "start": "08:00",
    "end": "09:40",
    "weeks": [1,2,3,4],
    "type": "OPTIONAL"
  },
  {
    "dayOfWeek": 3,
    "start": "14:00",
    "end": "15:40",
    "weeks": [1,2,3,4],
    "type": "FIXED"
  }
]
```

字段说明：
- `dayOfWeek`：1-7（周一到周日）
- `start`/`end`：`HH:mm`
- `weeks`：周次数组（可选）
- `type`：`OPTIONAL`（可选时间段） / `FIXED`（固定时间段）

### 2.2 `constraints.params`（JSONB）
用于承载规则参数，支持多种约束类型。

示例：不想早课

```json
{ "earliestStart": "10:00" }
```

示例：不想周五上课

```json
{ "bannedDayOfWeek": 5 }
```

## 3. DDL（可直接执行）

```sql
-- PostgreSQL 15+
-- UUID 生成函数
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 排课任务状态
DO $$ BEGIN
  CREATE TYPE schedule_status AS ENUM ('QUEUED','RUNNING','COMPLETED','FAILED');
EXCEPTION
  WHEN duplicate_object THEN null;
END $$;

-- 课程表：课程基础信息 + 灵活时间段
CREATE TABLE IF NOT EXISTS courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  teacher TEXT,
  location TEXT,
  -- 灵活时间段描述，JSONB（见文档 2.1）
  time_slots JSONB NOT NULL DEFAULT '[]'::jsonb,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_courses_name ON courses (name);

-- 用户约束/偏好：使用 JSONB 承载参数，weight 用于 Timefold 评分权重
CREATE TABLE IF NOT EXISTS constraints (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  -- 约束类型（由后端枚举/常量定义，例如: NO_EARLY_CLASS, NO_FRIDAY, MIN_GAP, ...）
  constraint_key TEXT NOT NULL,

  -- 约束权重（可正可负；建议后端定义语义：越大越重要）
  weight INTEGER NOT NULL DEFAULT 0,

  -- 是否启用
  enabled BOOLEAN NOT NULL DEFAULT true,

  -- JSONB 参数（见文档 2.2）
  params JSONB NOT NULL DEFAULT '{}'::jsonb,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_constraints_key ON constraints (constraint_key);

-- 排课任务与结果
CREATE TABLE IF NOT EXISTS schedules (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  status schedule_status NOT NULL DEFAULT 'QUEUED',

  -- 触发生成时刻
  requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  started_at TIMESTAMPTZ,
  finished_at TIMESTAMPTZ,

  -- 输入快照：生成时使用的 courses / constraints（用于可追溯）
  input_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,

  -- Timefold 得分（可存为对象，如 {"hard":0,"soft":-12} 或字符串）
  score JSONB NOT NULL DEFAULT '{}'::jsonb,

  -- 排课结果（建议结构：entries 数组，每项包含 courseId, dayOfWeek, start, end, ...）
  result JSONB NOT NULL DEFAULT '{}'::jsonb,

  -- 失败原因（status=FAILED 时）
  error_message TEXT,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_schedules_status ON schedules (status);
CREATE INDEX IF NOT EXISTS idx_schedules_requested_at ON schedules (requested_at DESC);
```

## 4. 结果 JSON（建议）
`schedules.result` 建议结构：

```json
{
  "entries": [
    {
      "courseId": "...",
      "courseName": "...",
      "dayOfWeek": 1,
      "start": "08:00",
      "end": "09:40",
      "location": "...",
      "teacher": "..."
    }
  ]
}
```

## 5. 重要约束（建议，由应用层保证）
- `updated_at` 在更新时由应用层写入当前时间。
- `constraints.constraint_key` 必须来自后端定义的受控集合（避免前端随意拼写）。
