package com.smartcoursescheduler.backend.solver.domain;

public enum ConstraintType {
    NO_MORNING_CLASSES,
    NO_FRIDAY,
    MINIMIZE_GAPS,
    PREFER_COMPACT_DAYS,
    MAX_DAYS_OFF,
    CONSECUTIVE_CLASSES,
    AVOID_LONG_CONTINUOUS,
    PREFER_FREE_LUNCH,
    AVOID_LATE_END,
    PREFER_MORNING,
    PREFER_AFTERNOON
}
