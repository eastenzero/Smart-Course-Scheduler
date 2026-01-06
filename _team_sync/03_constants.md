# 核心常量与枚举（共同语言 / SSOT）

本文档用于定义前后端共享的“共同语言”，包括：
- 约束类型（ConstraintType）枚举：前端展示 Label（中文）与后端 Key（英文大写）
- 时间槽（节次）规范：统一“第几节课”与具体时间段的映射

> 约束数据存储：参见 `01_database_schema.md` 中 `constraints.constraint_key` 与 `constraints.params`。

## 1. ConstraintType 枚举
### 1.1 字段约定
- `constraintKey`：后端使用 Key（英文大写，下划线分隔），写入 `constraints.constraint_key`
- `labelZh`：前端展示中文
- `weight`：整数；建议语义：
  - 数值越大代表用户越在意（软约束更强）
  - 0 代表无偏好
- `enabled`：是否启用
- `params`：JSON 对象；不同约束类型有不同参数

### 1.2 支持的约束类型列表
| constraintKey (Backend) | labelZh (Frontend) | 约束说明 | params（JSONB）示例 |
|---|---|---|---|
| NO_MORNING_CLASSES | 无早课 | 上午阈值之前安排课程则惩罚 | `{ "morningEnd": "12:00" }` |
| NO_FRIDAY | 不想周五上课 | 周五安排课程则惩罚 | `{ "bannedDayOfWeek": 5 }` |
| MINIMIZE_GAPS | 减少空课间隔 | 同一天内课程之间空档越多越惩罚 | `{ "maxGapMinutes": 60 }` |
| PREFER_COMPACT_DAYS | 希望课集中（少碎片） | 类似减少空档，但更强调紧凑 | `{ "maxGapMinutes": 45 }` |
| MAX_DAYS_OFF | 最大化休息天数 | 希望一周内尽量多空闲日 | `{ "minDaysOff": 2 }` |
| CONSECUTIVE_CLASSES | 连堂限制 | 连续上课节数超过阈值则惩罚 | `{ "maxConsecutiveClasses": 2 }` |
| AVOID_LONG_CONTINUOUS | 避免连续上课太久 | 连续课程时长超过阈值则惩罚 | `{ "maxContinuousMinutes": 180 }` |
| PREFER_FREE_LUNCH | 希望有午休窗口 | 午间窗口内至少留出一定空闲分钟数 | `{ "windowStart": "11:30", "windowEnd": "13:00", "minFreeMinutes": 40 }` |
| AVOID_LATE_END | 不想太晚下课 | 课程结束时间晚于阈值则惩罚 | `{ "latestEnd": "18:00" }` |
| PREFER_MORNING | 偏好上午上课 | 上午时段安排越多越奖励（或下午惩罚） | `{ "morningEnd": "12:00" }` |
| PREFER_AFTERNOON | 偏好下午上课 | 下午时段安排越多越奖励 | `{ "afternoonStart": "13:00" }` |

#### 1.2.1 说明
- 上表为“系统支持的约束集合（受控枚举）”。
- 任何新增/删除/重命名 `constraintKey` 必须先更新本文件，再由后端实现并由前端同步。

### 1.3 Deprecated / Legacy（历史兼容）
以下 Key 仅用于兼容历史数据，不应再由前端/接口写入。

| legacyKey | replacementKey | 说明 |
|---|---|---|
| NO_EARLY_CLASS | NO_MORNING_CLASSES | 已统一使用新标准；数据库通过 Flyway `V3__standardize_constraint_keys.sql` 自动迁移 |
| MAXIMIZE_DAYS_OFF | MAX_DAYS_OFF | 统一命名；数据库通过 Flyway `V3__standardize_constraint_keys.sql` 自动迁移 |

## 2. 时间槽（节次）规范
### 2.1 目标
- 前端可用“第几节课”进行展示与选取。
- 后端/Timefold 可用节次快速做冲突判断，同时仍可兼容 `time_slots` 中的 `HH:mm` 描述。

### 2.2 基本规则
- 一天共 12 节（SLOT_1 ~ SLOT_12）。
- 单节课时长：45 分钟。
- 节间休息：10 分钟（除特殊休息段外）。
- 午休：第 5 节与第 6 节之间为较长休息（示例安排）。

> 如学校实际作息不同，可在此处统一调整。调整后前后端必须同时更新。

### 2.3 节次时间表（默认）
| slotKey | 节次 | start | end |
|---|---:|---|---|
| SLOT_1 | 1 | 08:00 | 08:45 |
| SLOT_2 | 2 | 08:55 | 09:40 |
| SLOT_3 | 3 | 10:00 | 10:45 |
| SLOT_4 | 4 | 10:55 | 11:40 |
| SLOT_5 | 5 | 13:00 | 13:45 |
| SLOT_6 | 6 | 13:55 | 14:40 |
| SLOT_7 | 7 | 15:00 | 15:45 |
| SLOT_8 | 8 | 15:55 | 16:40 |
| SLOT_9 | 9 | 17:00 | 17:45 |
| SLOT_10 | 10 | 17:55 | 18:40 |
| SLOT_11 | 11 | 19:00 | 19:45 |
| SLOT_12 | 12 | 19:55 | 20:40 |

### 2.4 与 `courses.time_slots` 的关系
- 数据库存储仍使用 `HH:mm` 的 `start/end`。
- 前端如以“节次”录入，则需按本表将节次转换为 `start/end` 写入 `time_slots`。
- 后端如需将 `HH:mm` 映射回节次用于展示：
  - 仅当 `start/end` 与表中完全一致时可反向映射；否则按“自定义时间段”展示。
