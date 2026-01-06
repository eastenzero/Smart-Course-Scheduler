# curl 手动测试示例

> 默认后端地址：`http://localhost:8080`。
> 如需切换，请先设置：
>
> ```bash
> export BASE_URL=http://localhost:8080
> ```

## 1) 查看课程列表

```bash
curl -sS "$BASE_URL/api/courses" \
  -H 'Accept: application/json'
```

## 2) 添加一个排课约束（无早课：NO_MORNING_CLASSES）

> SSOT 接口字段名为 `constraintKey`（见 `02_api_spec.md`）。
> 若后端实现采用 `type` 作为别名字段，也可使用下方“兼容写法”。

```bash
curl -sS -X POST "$BASE_URL/api/constraints" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "constraints": [
      {
        "constraintKey": "NO_MORNING_CLASSES",
        "weight": 10,
        "enabled": true,
        "params": { "morningEnd": "10:00" }
      }
    ]
  }'
```

兼容写法（仅当后端支持 `type`）：

```bash
curl -sS -X POST "$BASE_URL/api/constraints" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "constraints": [
      {
        "type": "NO_MORNING_CLASSES",
        "weight": 10,
        "enabled": true,
        "params": { "morningEnd": "10:00" }
      }
    ]
  }'
```

## 3) 触发排课（异步，返回 scheduleId）

> 自动从 `/api/courses` 抓取所有课程 ID，拼成 `courseIds` 数组（不依赖 jq）。

```bash
COURSE_IDS_JSON=$(curl -sS "$BASE_URL/api/courses" \
  -H 'Accept: application/json' | \
  python3 -c 'import sys, json; data=json.load(sys.stdin); print(json.dumps([c["id"] for c in data.get("courses", [])]))')

curl -sS -X POST "$BASE_URL/api/schedule/generate" \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "courseIds": '"$COURSE_IDS_JSON"',
    "options": { "maxSeconds": 10 }
  }'
```

## 4) 查询状态（轮询用）

> 将 `SCHEDULE_ID` 替换为第 3 步返回的 `scheduleId`。

```bash
SCHEDULE_ID="<paste-scheduleId-here>"

curl -sS "$BASE_URL/api/schedule/status?scheduleId=$SCHEDULE_ID" \
  -H 'Accept: application/json'
```

## （可选）获取最终结果

```bash
SCHEDULE_ID="<paste-scheduleId-here>"

curl -sS "$BASE_URL/api/schedule/result?scheduleId=$SCHEDULE_ID" \
  -H 'Accept: application/json'
```
