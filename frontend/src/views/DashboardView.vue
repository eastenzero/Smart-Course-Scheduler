
<template>
  <div class="dashboard-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">排课总览</h1>
        <p class="page-desc">AI 驱动的智能课表生成 — 一键生成最优方案</p>
      </div>
      <div class="header-actions">
        <button class="btn-secondary" @click="fetchSchedule()" :disabled="isPolling">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
          刷新
        </button>
        <button class="btn-primary" @click="startGeneration" :disabled="isPolling">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
          </svg>
          {{ isPolling ? '生成中...' : '生成排课' }}
        </button>
      </div>
    </div>

    <!-- Stats Row -->
    <div class="stats-row" v-if="schedule">
      <div class="stat-card surface-card">
        <span class="stat-value">{{ entries.length }}</span>
        <span class="stat-label">课程排列</span>
      </div>
      <div class="stat-card surface-card">
        <span class="stat-value">{{ uniqueDays }}</span>
        <span class="stat-label">上课天数</span>
      </div>
      <div class="stat-card surface-card" v-if="schedule.score">
        <span class="stat-value">{{ schedule.score.hard }}</span>
        <span class="stat-label">Hard Score</span>
      </div>
      <div class="stat-card surface-card" v-if="schedule.score">
        <span class="stat-value">{{ schedule.score.soft }}</span>
        <span class="stat-label">Soft Score</span>
      </div>
    </div>

    <!-- Generation Overlay -->
    <transition name="page-fade">
      <div v-if="isPolling" class="gen-card surface-card">
        <div class="gen-content">
          <div class="gen-spinner">
            <svg class="spinner-ring" viewBox="0 0 50 50">
              <circle cx="25" cy="25" r="20" fill="none" stroke="currentColor" stroke-width="3" opacity="0.15"/>
              <circle cx="25" cy="25" r="20" fill="none" stroke="currentColor" stroke-width="3" stroke-dasharray="80" stroke-dashoffset="60" stroke-linecap="round" class="spinner-path"/>
            </svg>
          </div>
          <h3>AI 正在计算最优方案</h3>
          <p class="gen-desc">正在分析课程约束、时间冲突与偏好设置...</p>
          <div class="progress-bar">
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: progress + '%' }"/>
            </div>
            <span class="progress-text">{{ progress }}%</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- Loading Skeleton -->
    <div v-if="loading && !isPolling" class="skeleton-area">
      <div class="skeleton" style="height:60px; margin-bottom:16px"/>
      <div class="skeleton" style="height:400px"/>
    </div>

    <!-- Empty State -->
    <div v-else-if="!schedule && !isPolling && !loading" class="empty-state surface-card">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
        <path d="M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01"/>
      </svg>
      <h3>尚未生成课表</h3>
      <p>点击上方「生成排课」按钮，AI 将根据你的偏好自动生成最优课表方案。</p>
      <button class="btn-primary" @click="startGeneration" style="margin-top:16px">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
        </svg>
        立即生成
      </button>
    </div>

    <!-- Schedule Result -->
    <div v-else-if="schedule && !isPolling" class="result-section">
      <div class="result-header">
        <h2>课程排列表</h2>
        <div class="result-meta">
          <span class="status-tag" :class="'status-' + getStatusClass(schedule.status)">
            {{ schedule.status }}
          </span>
          <span v-if="schedule.errorMessage" class="error-msg">{{ schedule.errorMessage }}</span>
        </div>
      </div>
      <div class="grid-wrapper surface-card">
        <ScheduleGrid :entries="entries" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import {
    getScheduleResult,
    triggerSchedule,
    getScheduleStatus,
    type ScheduleResultResponse
} from '@/api/modules/schedule'
import ScheduleGrid, { type ScheduleEntry } from '@/components/ScheduleGrid.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const schedule = ref<ScheduleResultResponse['schedule'] | null>(null)

const isPolling = ref(false)
const progress = ref(0)
const pollingInterval = ref<any>(null)
const currentScheduleId = ref<string | null>(null)

const entries = computed(() => {
    return (schedule.value?.result?.entries || []) as ScheduleEntry[]
})

const uniqueDays = computed(() => {
    const days = new Set(entries.value.map(e => e.dayOfWeek))
    return days.size
})

const getStatusClass = (status: string) => {
    if (status === 'COMPLETED' || status === 'SOLVED') return 'success'
    if (status === 'RUNNING' || status === 'SOLVING') return 'warning'
    if (status === 'FAILED' || status === 'ERROR') return 'danger'
    return 'info'
}

const fetchSchedule = async (scheduleId?: string) => {
    loading.value = true
    try {
        const res = await getScheduleResult(scheduleId)
        schedule.value = res.data.schedule
    } catch (e: any) {
        if (e.response && e.response.status === 404) {
            schedule.value = null
        } else {
            console.error('Failed to fetch schedule', e)
        }
    } finally {
        loading.value = false
    }
}

const startGeneration = async () => {
    try {
        isPolling.value = true
        progress.value = 0
        const res = await triggerSchedule({ courseIds: [] })
        currentScheduleId.value = res.data.scheduleId
        startPolling(currentScheduleId.value)
    } catch (e) {
        console.error('Failed to trigger generation', e)
        ElMessage.error('排课生成启动失败')
        isPolling.value = false
    }
}

const startPolling = (scheduleId: string) => {
    if (pollingInterval.value) clearInterval(pollingInterval.value)
    pollingInterval.value = setInterval(async () => {
        if (progress.value < 90) progress.value += 5
        try {
            const res = await getScheduleStatus(scheduleId)
            const status = res.data.status
            if (status === 'SOLVED' || status === 'COMPLETED') {
                stopPolling()
                progress.value = 100
                ElMessage.success('排课生成成功！')
                fetchSchedule(scheduleId)
            } else if (status === 'ERROR' || status === 'FAILED') {
                stopPolling()
                ElMessage.error('排课生成失败')
            }
        } catch (e) {
            console.error('Polling error', e)
        }
    }, 1500)
}

const stopPolling = () => {
    if (pollingInterval.value) clearInterval(pollingInterval.value)
    pollingInterval.value = null
    isPolling.value = false
}

onMounted(() => { fetchSchedule() })
onUnmounted(() => { stopPolling() })
</script>

<style scoped>
.dashboard-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* ── Page Header ────────────────────────────────── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--sp-6);
  flex-wrap: wrap;
  gap: var(--sp-4);
}

.page-title {
  font-size: var(--text-xl);
  margin-bottom: var(--sp-1);
}

.page-desc {
  color: var(--c-text-secondary);
  font-size: var(--text-sm);
}

.header-actions {
  display: flex;
  gap: var(--sp-2);
  flex-shrink: 0;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  padding: 8px 20px;
  background: var(--c-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: background var(--duration-fast) ease;
}
.btn-primary:hover:not(:disabled) { background: var(--c-primary-dark); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  padding: 8px 16px;
  background: var(--c-surface);
  color: var(--c-text-secondary);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: border-color var(--duration-fast) ease;
}
.btn-secondary:hover:not(:disabled) { border-color: var(--c-primary); color: var(--c-primary); }
.btn-secondary:disabled { opacity: 0.5; cursor: not-allowed; }

/* ── Stats ──────────────────────────────────────── */
.stats-row {
  display: flex;
  gap: var(--sp-4);
  margin-bottom: var(--sp-6);
  flex-wrap: wrap;
}

.stat-card {
  padding: var(--sp-4) var(--sp-5);
  min-width: 120px;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--c-primary);
  font-family: var(--font-mono);
  line-height: 1.2;
}

.stat-label {
  display: block;
  font-size: var(--text-xs);
  color: var(--c-text-muted);
  margin-top: var(--sp-1);
}

/* ── Generation Card ────────────────────────────── */
.gen-card {
  padding: var(--sp-10) var(--sp-8);
  text-align: center;
  max-width: 480px;
  margin: var(--sp-8) auto;
}

.gen-spinner {
  width: 48px;
  height: 48px;
  margin: 0 auto var(--sp-4);
  color: var(--c-primary);
}

.spinner-ring {
  width: 100%;
  height: 100%;
  animation: spin 1.2s linear infinite;
}

.spinner-path {
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes spin {
  100% { transform: rotate(360deg); }
}
@keyframes dash {
  0% { stroke-dashoffset: 80; }
  50% { stroke-dashoffset: 20; }
  100% { stroke-dashoffset: 80; }
}

.gen-desc {
  color: var(--c-text-muted);
  font-size: var(--text-sm);
  margin-top: var(--sp-2);
}

.progress-bar {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-top: var(--sp-6);
  max-width: 300px;
  margin-left: auto;
  margin-right: auto;
}

.progress-track {
  flex: 1;
  height: 6px;
  background: var(--c-border-light);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--c-primary);
  border-radius: var(--radius-full);
  transition: width 300ms ease;
}

.progress-text {
  font-size: var(--text-xs);
  color: var(--c-text-muted);
  font-family: var(--font-mono);
  min-width: 36px;
}

/* ── Loading ────────────────────────────────────── */
.skeleton-area {
  margin-top: var(--sp-4);
}

/* ── Empty State ────────────────────────────────── */
.empty-state {
  text-align: center;
  padding: var(--sp-12) var(--sp-6);
  margin-top: var(--sp-4);
}
.empty-state svg {
  color: var(--c-text-muted);
  margin-bottom: var(--sp-4);
  opacity: 0.5;
}
.empty-state h3 {
  font-size: var(--text-base);
  margin-bottom: var(--sp-2);
}
.empty-state p {
  font-size: var(--text-sm);
  color: var(--c-text-secondary);
  max-width: 360px;
  margin: 0 auto;
  line-height: 1.6;
}

/* ── Result ─────────────────────────────────────── */
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-4);
  flex-wrap: wrap;
  gap: var(--sp-3);
}

.result-header h2 {
  font-size: var(--text-lg);
}

.result-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
}

.status-tag {
  font-size: var(--text-xs);
  font-weight: 600;
  padding: 2px 10px;
  border-radius: var(--radius-full);
}
.status-success { background: var(--c-success-light); color: var(--c-success); }
.status-warning { background: var(--c-warning-light); color: var(--c-warning); }
.status-danger  { background: var(--c-danger-light); color: var(--c-danger); }
.status-info    { background: var(--c-primary-50); color: var(--c-primary); }

.error-msg {
  font-size: var(--text-xs);
  color: var(--c-danger);
}

.grid-wrapper {
  padding: var(--sp-4);
  overflow-x: auto;
}

/* ── Responsive ─────────────────────────────────── */
@media (max-width: 600px) {
  .page-header {
    flex-direction: column;
  }
  .header-actions {
    width: 100%;
  }
  .btn-primary, .btn-secondary {
    flex: 1;
    justify-content: center;
  }
  .stats-row {
    gap: var(--sp-2);
  }
  .stat-card {
    flex: 1;
    min-width: 80px;
    padding: var(--sp-3);
  }
}
</style>
