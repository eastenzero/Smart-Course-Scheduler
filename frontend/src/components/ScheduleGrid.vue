
<template>
  <div class="schedule-grid">
    <div class="grid-header">
      <div class="time-column-header"></div>
      <div v-for="day in days" :key="day.value" class="day-header">
        {{ day.label }}
      </div>
    </div>
    <div class="grid-body">
      <div class="time-column">
        <!-- Render Slot Labels -->
        <div 
            v-for="slot in slots" 
            :key="slot.key" 
            class="time-label" 
            :style="{ top: getTopOffset(slot.start) + 'px', height: getSlotHeight(slot) + 'px' }"
        >
          <div class="slot-number">{{ slot.index }}</div>
          <div class="slot-time">{{ slot.start }}</div>
        </div>
      </div>
      <div class="days-column">
         <!-- Background Grid Lines for Slots -->
        <div 
            v-for="slot in slots" 
            :key="'line-' + slot.key" 
            class="grid-line" 
            :style="{ top: getTopOffset(slot.start) + 'px' }"
        ></div>

        <!-- Course Blocks -->
        <div
          v-for="entry in entries"
          :key="entry.courseId + entry.dayOfWeek + entry.start"
          class="course-block"
          :style="getBlockStyle(entry)"
        >
          <div class="course-content">
            <div class="course-name">{{ entry.courseName }}</div>
            <div class="course-time">{{ entry.start }} - {{ entry.end }}</div>
            <div class="course-location">{{ entry.location }}</div>
            <div class="course-teacher">{{ entry.teacher }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { TIME_SLOTS, DAYS_OF_WEEK } from '@/constants'

export interface ScheduleEntry {
  courseId: string
  courseName: string
  dayOfWeek: number // 1 = Mon, 7 = Sun
  start: string // HH:mm
  end: string // HH:mm
  location: string
  teacher: string
}

defineProps<{
  entries: ScheduleEntry[]
}>()

const PIXELS_PER_MINUTE = 1.3 // Adjusted for readability
const START_TIME_MINUTES = 8 * 60 // 08:00 start of grid

// Helper to parse "HH:mm" to minutes from midnight
function parseTime(timeStr: string): number {
  if (!timeStr) return 0
  const parts = timeStr.split(':')
  if (parts.length < 2) return 0
  
  const h = Number(parts[0])
  const m = Number(parts[1])
  
  if (isNaN(h) || isNaN(m)) return 0
  return h * 60 + m
}

// Get vertical offset relative to grid start (08:00)
function getTopOffset(timeStr: string): number {
  const minutes = parseTime(timeStr)
  return (minutes - START_TIME_MINUTES) * PIXELS_PER_MINUTE
}

function getSlotHeight(slot: typeof TIME_SLOTS[0]): number {
    const start = parseTime(slot.start)
    const end = parseTime(slot.end)
    return (end - start) * PIXELS_PER_MINUTE
}

function getBlockStyle(entry: ScheduleEntry) {
  const startMinutes = parseTime(entry.start)
  const endMinutes = parseTime(entry.end)
  
  const top = (startMinutes - START_TIME_MINUTES) * PIXELS_PER_MINUTE
  const height = (endMinutes - startMinutes) * PIXELS_PER_MINUTE

  // Calculate column position
  const dayIndex = entry.dayOfWeek - 1
  const widthPercent = 100 / 7
  const leftPercent = widthPercent * dayIndex

  // Determine color (simple hashing)
  const charCode = entry.courseName.charCodeAt(0) || 0
  const variants = ['blue', 'green', 'orange', 'red', 'purple'] as const
  const variantIndex = charCode % variants.length
  const variant = variants[variantIndex] || 'blue' // Fallback to ensure not undefined
  
  const styles: Record<typeof variants[number], { bg: string, border: string }> = {
      blue: { bg: '#ecf5ff', border: '#409eff' },
      green: { bg: '#f0f9eb', border: '#67c23a' },
      orange: { bg: '#fdf6ec', border: '#e6a23c' },
      red: { bg: '#fef0f0', border: '#f56c6c' },
      purple:{ bg: '#f4f4f5', border: '#909399' },
  }
  const theme = styles[variant]

  return {
    top: `${top}px`,
    height: `${height}px`,
    left: `${leftPercent}%`,
    width: `${widthPercent}%`,
    backgroundColor: theme.bg,
    borderLeft: `3px solid ${theme.border}`,
    color: '#303133'
  }
}

const days = DAYS_OF_WEEK
const slots = TIME_SLOTS
</script>

<style scoped>
.schedule-grid {
  display: flex;
  flex-direction: column;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  min-width: 800px;
}

.grid-header {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  background-color: #f5f7fa;
  position: sticky;
  top: 0;
  z-index: 20;
}

.time-column-header {
  width: 60px;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
}

.day-header {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-weight: bold;
  font-size: 14px;
  color: #606266;
  border-right: 1px solid #ebeef5;
}
.day-header:last-child {
  border-right: none;
}

.grid-body {
  display: flex;
  position: relative;
  height: 800px;
  overflow-y: auto;
}

.time-column {
  width: 60px;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  position: relative;
  background-color: #fafafa;
}

.time-label {
  position: absolute;
  width: 100%;
  text-align: center;
  font-size: 12px;
  color: #909399;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.slot-number {
    font-weight: bold;
    color: #606266;
}
.slot-time {
    font-size: 10px;
    transform: scale(0.9);
}

.days-column {
  flex: 1;
  position: relative;
}

.grid-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background-color: #ebeef5;
}

.course-block {
  position: absolute;
  box-sizing: border-box;
  padding: 4px 8px;
  border-radius: 4px;
  overflow: hidden;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin: 1px;
  width: calc(100% / 7 - 2px) !important; 
  z-index: 1;
}

.course-block:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 10;
  transform: translateY(-1px);
}

.course-name {
  font-weight: bold;
  margin-bottom: 2px;
}
.course-time {
    font-size: 11px;
    opacity: 0.8;
}
</style>
