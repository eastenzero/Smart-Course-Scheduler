
<template>
  <div class="schedule-grid">
    <!-- Mobile scroll hint -->
    <div v-if="isMobile" class="scroll-hint">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="9 18 15 12 9 6"/>
      </svg>
      <span>横向滑动查看完整课表</span>
    </div>

    <div class="grid-scroll-wrapper">
      <div class="grid-container">
        <!-- Header Row -->
        <div class="grid-header">
          <div class="time-col-header">
            <span class="time-header-label">时段</span>
          </div>
          <div v-for="day in days" :key="day.value" class="day-header">
            <span class="day-short">{{ day.label }}</span>
            <span class="day-dot" :class="{ 'has-class': hasCourseOnDay(day.value) }" />
          </div>
        </div>

        <!-- Body -->
        <div class="grid-body" ref="gridBody">
          <!-- Time Column -->
          <div class="time-column">
            <div
              v-for="slot in slots"
              :key="slot.key"
              class="time-label"
              :style="{ top: getTopOffset(slot.start) + 'px', height: getSlotHeight(slot) + 'px' }"
            >
              <div class="slot-index">{{ slot.index }}</div>
              <div class="slot-time">{{ slot.start }}</div>
            </div>
          </div>

          <!-- Days Area -->
          <div class="days-area">
            <!-- Grid lines -->
            <div
              v-for="slot in slots"
              :key="'line-' + slot.key"
              class="grid-line"
              :style="{ top: getTopOffset(slot.start) + 'px' }"
            />

            <!-- Day column separators -->
            <div
              v-for="i in 6"
              :key="'sep-' + i"
              class="day-separator"
              :style="{ left: (100 / 7 * i) + '%' }"
            />

            <!-- Course Blocks -->
            <div
              v-for="(entry, idx) in entries"
              :key="entry.courseId + entry.dayOfWeek + entry.start"
              class="course-block"
              :style="getBlockStyle(entry, idx)"
              @mouseenter="hoveredEntry = entry"
              @mouseleave="hoveredEntry = null"
            >
              <div class="block-accent" :style="{ background: getAccentColor(idx) }" />
              <div class="block-content">
                <div class="block-name">{{ entry.courseName }}</div>
                <div class="block-time">{{ entry.start }} - {{ entry.end }}</div>
                <div class="block-meta" v-if="entry.location || entry.teacher">
                  <span v-if="entry.location" class="meta-item">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    {{ entry.location }}
                  </span>
                  <span v-if="entry.teacher" class="meta-item">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    {{ entry.teacher }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Tooltip / Popover -->
            <transition name="page-fade">
              <div
                v-if="hoveredEntry"
                class="course-tooltip surface-card"
                :style="getTooltipPosition()"
              >
                <div class="tooltip-header">{{ hoveredEntry.courseName }}</div>
                <div class="tooltip-row">
                  <span class="tooltip-label">时间</span>
                  <span>{{ getDayLabel(hoveredEntry.dayOfWeek) }} {{ hoveredEntry.start }} - {{ hoveredEntry.end }}</span>
                </div>
                <div class="tooltip-row" v-if="hoveredEntry.location">
                  <span class="tooltip-label">教室</span>
                  <span>{{ hoveredEntry.location }}</span>
                </div>
                <div class="tooltip-row" v-if="hoveredEntry.teacher">
                  <span class="tooltip-label">教师</span>
                  <span>{{ hoveredEntry.teacher }}</span>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { TIME_SLOTS, DAYS_OF_WEEK } from '@/constants'

export interface ScheduleEntry {
  courseId: string
  courseName: string
  dayOfWeek: number
  start: string
  end: string
  location: string
  teacher: string
}

const props = withDefaults(defineProps<{
  entries?: ScheduleEntry[]
}>(), {
  entries: () => []
})

const hoveredEntry = ref<ScheduleEntry | null>(null)
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)

const handleResize = () => { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

const PIXELS_PER_MINUTE = 1.3
const START_TIME_MINUTES = 8 * 60

function parseTime(timeStr: string): number {
  if (!timeStr) return 0
  const parts = timeStr.split(':')
  if (parts.length < 2) return 0
  const h = Number(parts[0])
  const m = Number(parts[1])
  if (isNaN(h) || isNaN(m)) return 0
  return h * 60 + m
}

function getTopOffset(timeStr: string): number {
  return (parseTime(timeStr) - START_TIME_MINUTES) * PIXELS_PER_MINUTE
}

function getSlotHeight(slot: typeof TIME_SLOTS[0]): number {
  return (parseTime(slot.end) - parseTime(slot.start)) * PIXELS_PER_MINUTE
}

// Harmonious color palette using HSL rotation
const COURSE_COLORS = [
  { bg: '#EFF6FF', accent: '#3B82F6', text: '#1E40AF' },     // Blue
  { bg: '#F0FDF4', accent: '#22C55E', text: '#166534' },     // Green
  { bg: '#FFF7ED', accent: '#F97316', text: '#9A3412' },     // Orange
  { bg: '#FDF2F8', accent: '#EC4899', text: '#9D174D' },     // Pink
  { bg: '#F5F3FF', accent: '#8B5CF6', text: '#5B21B6' },     // Violet
  { bg: '#ECFEFF', accent: '#06B6D4', text: '#155E75' },     // Cyan
  { bg: '#FFFBEB', accent: '#F59E0B', text: '#92400E' },     // Amber
  { bg: '#FEF2F2', accent: '#EF4444', text: '#991B1B' },     // Red
  { bg: '#F0FDFA', accent: '#14B8A6', text: '#115E59' },     // Teal
  { bg: '#EEF2FF', accent: '#6366F1', text: '#3730A3' },     // Indigo
]

function getColorForIndex(idx: number): { bg: string; accent: string; text: string } {
  return COURSE_COLORS[idx % COURSE_COLORS.length]!
}

function getAccentColor(idx: number): string {
  return getColorForIndex(idx).accent
}

function getBlockStyle(entry: ScheduleEntry, idx: number) {
  const startMin = parseTime(entry.start)
  const endMin = parseTime(entry.end)
  const top = (startMin - START_TIME_MINUTES) * PIXELS_PER_MINUTE
  const height = (endMin - startMin) * PIXELS_PER_MINUTE
  const dayIndex = entry.dayOfWeek - 1
  const widthPercent = 100 / 7
  const leftPercent = widthPercent * dayIndex
  const colors = getColorForIndex(idx) || COURSE_COLORS[0]

  return {
    top: `${top}px`,
    height: `${height}px`,
    left: `calc(${leftPercent}% + 2px)`,
    width: `calc(${widthPercent}% - 4px)`,
    backgroundColor: colors!.bg,
    color: colors!.text,
    '--block-accent': colors!.accent
  }
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function hasCourseOnDay(_dayValue: number): boolean {
  return false
}

function getDayLabel(dayOfWeek: number): string {
  return DAYS_OF_WEEK.find(d => d.value === dayOfWeek)?.label || ''
}

function getTooltipPosition() {
  return { bottom: '100%', right: '0', marginBottom: '8px' }
}

const days = DAYS_OF_WEEK
const slots = TIME_SLOTS
</script>

<style scoped>
/* ── Grid Container ─────────────────────────────── */
.schedule-grid {
  position: relative;
}

.scroll-hint {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  font-size: var(--text-xs);
  color: var(--c-text-muted);
  margin-bottom: var(--sp-2);
  animation: hint-bounce 2s ease-in-out infinite;
}

@keyframes hint-bounce {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(4px); }
}

.grid-scroll-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  border-radius: var(--radius-lg);
}

.grid-container {
  display: flex;
  flex-direction: column;
  min-width: 760px;
  background: var(--c-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--c-border-light);
}

/* ── Header ─────────────────────────────────────── */
.grid-header {
  display: flex;
  background: var(--c-primary-50);
  border-bottom: 1px solid var(--c-border);
  position: sticky;
  top: 0;
  z-index: 20;
}

.time-col-header {
  width: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--sp-3) 0;
  border-right: 1px solid var(--c-border-light);
}

.time-header-label {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--c-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.day-header {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--sp-3) 0;
  gap: var(--sp-1);
}

.day-short {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--c-text-primary);
}

.day-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--c-border);
  transition: background var(--duration-fast) ease;
}
.day-dot.has-class {
  background: var(--c-primary);
}

/* ── Grid Body ──────────────────────────────────── */
.grid-body {
  display: flex;
  position: relative;
  height: 780px;
  overflow-y: auto;
}

/* ── Time Column ────────────────────────────────── */
.time-column {
  width: 56px;
  flex-shrink: 0;
  border-right: 1px solid var(--c-border-light);
  position: relative;
  background: var(--c-surface);
}

.time-label {
  position: absolute;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
}

.slot-index {
  font-size: var(--text-xs);
  font-weight: 700;
  color: var(--c-text-primary);
  line-height: 1;
}

.slot-time {
  font-size: 11px;
  color: var(--c-text-muted);
  font-family: var(--font-mono);
}

/* ── Days Area ──────────────────────────────────── */
.days-area {
  flex: 1;
  position: relative;
}

.grid-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--c-border-light);
}

.day-separator {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1px;
  background: var(--c-border-light);
}

/* ── Course Block ───────────────────────────────── */
.course-block {
  position: absolute;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  z-index: 1;
  display: flex;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.course-block:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  z-index: 10;
}

.block-accent {
  width: 3px;
  flex-shrink: 0;
}

.block-content {
  padding: var(--sp-1) var(--sp-2);
  overflow: hidden;
  flex: 1;
  min-width: 0;
}

.block-name {
  font-size: 12px;
  font-weight: 700;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.block-time {
  font-size: 11px;
  opacity: 0.7;
  font-family: var(--font-mono);
  margin-top: 1px;
}

.block-meta {
  display: flex;
  flex-direction: column;
  gap: 1px;
  margin-top: 2px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 10px;
  opacity: 0.65;
}

/* ── Tooltip ────────────────────────────────────── */
.course-tooltip {
  position: absolute;
  z-index: 50;
  padding: var(--sp-3) var(--sp-4);
  min-width: 200px;
  pointer-events: none;
  font-size: var(--text-xs);
}

.tooltip-header {
  font-weight: 700;
  font-size: var(--text-sm);
  margin-bottom: var(--sp-2);
  color: var(--c-text-primary);
}

.tooltip-row {
  display: flex;
  gap: var(--sp-2);
  margin-bottom: var(--sp-1);
  color: var(--c-text-secondary);
}

.tooltip-label {
  font-weight: 600;
  color: var(--c-text-muted);
  min-width: 32px;
}

/* ── Responsive ─────────────────────────────────── */
@media (max-width: 767px) {
  .grid-container {
    min-width: 700px;
  }
}
</style>
