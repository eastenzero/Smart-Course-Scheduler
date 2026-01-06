#!/usr/bin/env python3
"""API smoke test for Smart Course Scheduler.

Requirements:
- Python 3
- requests

This script is intended to be run manually after backend Timefold integration is ready.

Notes about API endpoints:
- Step 1 uses GET /api/courses (NOT yet part of SSOT in 02_api_spec.md; backend must implement).
- Step 4 prefers GET /api/schedule/status (assumed endpoint; backend must implement).
  If missing, the script falls back to GET /api/schedule/result (which is in SSOT).

Constraint key note:
- SSOT standard key is NO_MORNING_CLASSES (see 03_constants.md).
- This script sends NO_MORNING_CLASSES by default.
"""

from __future__ import annotations

import os
import sys
import time
from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Tuple

import requests


BASE_URL = os.getenv("SCHEDULER_BASE_URL", "http://localhost:8080")

# Requested by task description (may differ from SSOT).
CONSTRAINT_KEY = os.getenv("SCHEDULER_CONSTRAINT_KEY", "NO_MORNING_CLASSES")

# For NO_MORNING_CLASSES heuristic: class start < MORNING_END => violation.
MORNING_END = os.getenv("SCHEDULER_MORNING_END", "12:00")

POLL_INTERVAL_SECONDS = float(os.getenv("SCHEDULER_POLL_INTERVAL", "1"))
POLL_TIMEOUT_SECONDS = float(os.getenv("SCHEDULER_POLL_TIMEOUT", "60"))


@dataclass
class Course:
    id: str
    name: str


def _parse_hhmm_to_minutes(hhmm: str) -> int:
    hh, mm = hhmm.split(":", 1)
    return int(hh) * 60 + int(mm)


def _extract_courses(payload: Any) -> List[Course]:
    if isinstance(payload, list):
        items = payload
    elif isinstance(payload, dict):
        if "courses" in payload and isinstance(payload["courses"], list):
            items = payload["courses"]
        else:
            # Best-effort: treat dict as a single course object
            items = [payload]
    else:
        raise ValueError(f"Unexpected /api/courses response type: {type(payload)}")

    courses: List[Course] = []
    for item in items:
        if not isinstance(item, dict):
            continue
        course_id = item.get("id")
        name = item.get("name") or item.get("courseName") or "(unknown)"
        if course_id:
            courses.append(Course(id=str(course_id), name=str(name)))
    return courses


def _request_json(session: requests.Session, method: str, url: str, **kwargs: Any) -> Any:
    resp = session.request(method, url, timeout=15, **kwargs)
    if resp.status_code >= 400:
        raise RuntimeError(f"HTTP {resp.status_code} for {method} {url}: {resp.text}")
    if not resp.text.strip():
        return None
    return resp.json()


def step1_fetch_courses(session: requests.Session) -> List[Course]:
    url = f"{BASE_URL}/api/courses"
    payload = _request_json(session, "GET", url)
    courses = _extract_courses(payload)
    print(f"[Step 1] courses count = {len(courses)}")
    if courses:
        print(f"         first course = {courses[0].id} {courses[0].name}")
    return courses


def step2_set_constraints(session: requests.Session) -> None:
    url = f"{BASE_URL}/api/constraints"

    # Typical constraint example.
    # Standard key is NO_MORNING_CLASSES with params.morningEnd.
    constraint = {
        "constraintKey": CONSTRAINT_KEY,
        "weight": 10,
        "enabled": True,
        "params": {"morningEnd": MORNING_END},
    }
    body = {"constraints": [constraint]}

    try:
        _request_json(session, "POST", url, json=body)
    except RuntimeError as e:
        if "HTTP 400" not in str(e):
            raise

        fallback_body = {
            "constraintKey": CONSTRAINT_KEY,
            "weight": 10,
            "enabled": True,
            "params": {"morningEnd": MORNING_END},
        }
        _request_json(session, "POST", url, json=fallback_body)

    print(f"[Step 2] constraints saved: {CONSTRAINT_KEY} weight=10")


def step3_generate_schedule(session: requests.Session, course_ids: List[str]) -> str:
    url = f"{BASE_URL}/api/schedule/generate"
    body = {
        "courseIds": course_ids,
        "options": {
            "maxSeconds": 10,
        },
    }
    payload = _request_json(session, "POST", url, json=body)

    if not isinstance(payload, dict) or "scheduleId" not in payload:
        raise RuntimeError(f"Unexpected generate response: {payload}")

    schedule_id = str(payload["scheduleId"])
    status = payload.get("status")
    print(f"[Step 3] schedule generated: scheduleId={schedule_id} status={status}")
    return schedule_id


def _try_get_status_endpoint(session: requests.Session, schedule_id: str) -> Optional[Dict[str, Any]]:
    # Assumed endpoint (backend needs to implement):
    # GET /api/schedule/status?scheduleId=...
    url = f"{BASE_URL}/api/schedule/status"
    try:
        resp = session.get(url, params={"scheduleId": schedule_id}, timeout=10)
    except requests.RequestException:
        return None

    if resp.status_code == 404:
        return None
    if resp.status_code >= 400:
        raise RuntimeError(f"HTTP {resp.status_code} for GET {url}: {resp.text}")
    return resp.json() if resp.text.strip() else {}


def _get_result_endpoint(session: requests.Session, schedule_id: str) -> Dict[str, Any]:
    # SSOT endpoint:
    # GET /api/schedule/result?scheduleId=...
    url = f"{BASE_URL}/api/schedule/result"
    payload = _request_json(session, "GET", url, params={"scheduleId": schedule_id})
    if not isinstance(payload, dict):
        raise RuntimeError(f"Unexpected result response: {payload}")
    return payload


def step4_poll_until_solved(session: requests.Session, schedule_id: str) -> Dict[str, Any]:
    start = time.time()

    while True:
        elapsed = time.time() - start
        if elapsed > POLL_TIMEOUT_SECONDS:
            raise TimeoutError(f"Polling timeout after {POLL_TIMEOUT_SECONDS}s")

        status_payload = _try_get_status_endpoint(session, schedule_id)
        if status_payload is not None:
            # Expected shape (assumed): {"scheduleId":"...","status":"SOLVED"}
            status = str(status_payload.get("status", "")).upper()
            print(f"[Step 4] status={status} (via /api/schedule/status)")
            if status in {"SOLVED", "COMPLETED"}:
                return status_payload
            if status in {"FAILED", "ERROR"}:
                raise RuntimeError(f"Schedule failed: {status_payload}")
        else:
            result_payload = _get_result_endpoint(session, schedule_id)
            schedule = result_payload.get("schedule") if isinstance(result_payload, dict) else None
            if not isinstance(schedule, dict):
                raise RuntimeError(f"Unexpected schedule wrapper: {result_payload}")

            status = str(schedule.get("status", "")).upper()
            print(f"[Step 4] status={status} (fallback /api/schedule/result)")

            if status in {"SOLVED", "COMPLETED"}:
                return schedule
            if status in {"FAILED", "ERROR"}:
                raise RuntimeError(f"Schedule failed: {schedule.get('errorMessage') or schedule}")

        time.sleep(POLL_INTERVAL_SECONDS)


def _extract_entries(schedule_payload: Dict[str, Any]) -> List[Dict[str, Any]]:
    # schedule_payload could be:
    # - from /api/schedule/result: {id,status,score,result:{entries:[...]}}
    # - from /api/schedule/status: could include result too
    result = schedule_payload.get("result")
    if isinstance(result, dict) and isinstance(result.get("entries"), list):
        return [e for e in result["entries"] if isinstance(e, dict)]

    # Some backends might return entries at top-level.
    if isinstance(schedule_payload.get("entries"), list):
        return [e for e in schedule_payload["entries"] if isinstance(e, dict)]

    return []


def step5_print_summary(schedule_payload: Dict[str, Any]) -> None:
    entries = _extract_entries(schedule_payload)

    monday_count = 0
    violations: List[Tuple[str, str, str]] = []  # (courseName, dayOfWeek, start)

    morning_end_minutes = _parse_hhmm_to_minutes(MORNING_END)

    for e in entries:
        day = e.get("dayOfWeek")
        start = e.get("start")
        course_name = e.get("courseName") or e.get("name") or "(unknown)"

        if day == 1:
            monday_count += 1

        if CONSTRAINT_KEY == "NO_MORNING_CLASSES" and isinstance(start, str):
            try:
                start_m = _parse_hhmm_to_minutes(start)
                if start_m < morning_end_minutes:
                    violations.append((str(course_name), str(day), start))
            except Exception:
                pass

    print("\n[Step 5] Schedule summary")
    print(f"- entries total: {len(entries)}")
    print(f"- Monday (dayOfWeek=1) entries: {monday_count}")

    if CONSTRAINT_KEY == "NO_MORNING_CLASSES":
        print(f"- Constraint check: {CONSTRAINT_KEY} (morningEnd={MORNING_END})")
        print(f"  violation count: {len(violations)}")
        if violations:
            print("  first violations:")
            for course_name, day, start in violations[:10]:
                print(f"    - day={day} start={start} course={course_name}")


def main() -> int:
    print(f"BASE_URL={BASE_URL}")

    session = requests.Session()
    session.headers.update({"Accept": "application/json"})

    # Step 1
    courses = step1_fetch_courses(session)
    if not courses:
        print("No courses found. Cannot generate schedule.")
        return 2

    # Step 2
    step2_set_constraints(session)

    # Step 3
    schedule_id = step3_generate_schedule(session, [c.id for c in courses])

    # Step 4
    schedule_payload = step4_poll_until_solved(session, schedule_id)

    # Step 5
    step5_print_summary(schedule_payload)

    print("\nOK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
