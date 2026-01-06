
// Derived from _team_sync/03_constants.md

// 1. ConstraintType Enum
export const ConstraintType = {
    NO_EARLY_CLASS: 'NO_EARLY_CLASS',
    NO_FRIDAY: 'NO_FRIDAY',
    MINIMIZE_GAPS: 'MINIMIZE_GAPS',
    PREFER_COMPACT_DAYS: 'PREFER_COMPACT_DAYS',
    MAXIMIZE_DAYS_OFF: 'MAXIMIZE_DAYS_OFF',
    AVOID_LONG_CONTINUOUS: 'AVOID_LONG_CONTINUOUS',
    PREFER_FREE_LUNCH: 'PREFER_FREE_LUNCH',
    AVOID_LATE_END: 'AVOID_LATE_END',
    PREFER_MORNING: 'PREFER_MORNING',
    PREFER_AFTERNOON: 'PREFER_AFTERNOON',
} as const;

export type ConstraintType = (typeof ConstraintType)[keyof typeof ConstraintType];

export const ConstraintLabels: Record<ConstraintType, string> = {
    [ConstraintType.NO_EARLY_CLASS]: '不想上早课',
    [ConstraintType.NO_FRIDAY]: '不想周五上课',
    [ConstraintType.MINIMIZE_GAPS]: '减少空课间隔',
    [ConstraintType.PREFER_COMPACT_DAYS]: '希望课集中（少碎片）',
    [ConstraintType.MAXIMIZE_DAYS_OFF]: '最大化休息天数',
    [ConstraintType.AVOID_LONG_CONTINUOUS]: '避免连续上课太久',
    [ConstraintType.PREFER_FREE_LUNCH]: '希望有午休窗口',
    [ConstraintType.AVOID_LATE_END]: '不想太晚下课',
    [ConstraintType.PREFER_MORNING]: '偏好上午上课',
    [ConstraintType.PREFER_AFTERNOON]: '偏好下午上课',
}

// 2. Time Slots
export interface TimeSlot {
    key: string
    index: number
    start: string // HH:mm
    end: string // HH:mm
}

export const TIME_SLOTS: TimeSlot[] = [
    { key: 'SLOT_1', index: 1, start: '08:00', end: '08:45' },
    { key: 'SLOT_2', index: 2, start: '08:55', end: '09:40' },
    { key: 'SLOT_3', index: 3, start: '10:00', end: '10:45' },
    { key: 'SLOT_4', index: 4, start: '10:55', end: '11:40' },
    { key: 'SLOT_5', index: 5, start: '13:00', end: '13:45' },
    { key: 'SLOT_6', index: 6, start: '13:55', end: '14:40' },
    { key: 'SLOT_7', index: 7, start: '15:00', end: '15:45' },
    { key: 'SLOT_8', index: 8, start: '15:55', end: '16:40' },
    { key: 'SLOT_9', index: 9, start: '17:00', end: '17:45' },
    { key: 'SLOT_10', index: 10, start: '17:55', end: '18:40' },
    { key: 'SLOT_11', index: 11, start: '19:00', end: '19:45' },
    { key: 'SLOT_12', index: 12, start: '19:55', end: '20:40' },
]

export const DAYS_OF_WEEK = [
    { label: 'Mon', value: 1 },
    { label: 'Tue', value: 2 },
    { label: 'Wed', value: 3 },
    { label: 'Thu', value: 4 },
    { label: 'Fri', value: 5 },
    { label: 'Sat', value: 6 },
    { label: 'Sun', value: 7 },
]
