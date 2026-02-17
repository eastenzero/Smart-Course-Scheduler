
<template>
  <div class="admin-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">课程管理</h1>
        <p class="page-desc">查看系统中所有已配置的课程信息</p>
      </div>
      <button class="btn-refresh" @click="fetchCourses" :disabled="loading">
        <svg v-if="!loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
          <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
        </svg>
        <span class="spinner-icon" v-else />
        刷新
      </button>
    </div>

    <!-- Search Bar -->
    <div class="search-bar surface-card">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
      </svg>
      <input
        v-model="searchQuery"
        type="text"
        placeholder="搜索课程名称、教师、教室..."
        class="search-input"
      />
      <span v-if="searchQuery" class="search-clear" @click="searchQuery = ''">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </span>
    </div>

    <!-- Stats -->
    <div class="stats-mini" v-if="courses.length > 0">
      <div class="stat-pill">
        <span class="stat-num">{{ filteredCourses.length }}</span>
        <span class="stat-txt">门课程</span>
      </div>
      <div class="stat-pill">
        <span class="stat-num">{{ totalCredits }}</span>
        <span class="stat-txt">总学分</span>
      </div>
      <div class="stat-pill">
        <span class="stat-num">{{ uniqueTeachers }}</span>
        <span class="stat-txt">教师</span>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="skeleton-area">
      <div class="skeleton skeleton-card" v-for="i in 4" :key="i" style="height:56px" />
    </div>

    <!-- Empty State -->
    <div v-else-if="courses.length === 0" class="empty-state surface-card">
      <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
      </svg>
      <h3>暂无课程数据</h3>
      <p>请确认后端服务已启动，或尝试刷新页面</p>
    </div>

    <!-- Desktop Table -->
    <div v-else class="table-wrapper surface-card desktop-only">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width:70px">ID</th>
            <th>课程名称</th>
            <th style="width:100px">教师</th>
            <th style="width:70px; text-align:center">学分</th>
            <th style="width:100px">教室</th>
            <th>上课时间</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="course in filteredCourses"
            :key="course.id"
            class="table-row"
          >
            <td class="cell-id">{{ course.id }}</td>
            <td class="cell-name">{{ course.name }}</td>
            <td>{{ course.teacher }}</td>
            <td class="cell-credits">
              <span class="credit-badge" :class="'credit-' + Math.min(course.credits ?? 0, 5)">
                {{ course.credits ?? 0 }}
              </span>
            </td>
            <td>{{ course.location || '-' }}</td>
            <td class="cell-slots">{{ formatTimeSlots(course.timeSlots) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Mobile Card View -->
    <div v-if="!loading && courses.length > 0" class="mobile-cards mobile-only">
      <div
        v-for="course in filteredCourses"
        :key="'m-' + course.id"
        class="course-card surface-card"
      >
        <div class="card-top">
          <span class="card-name">{{ course.name }}</span>
          <span class="credit-badge" :class="'credit-' + Math.min(course.credits ?? 0, 5)">
            {{ course.credits ?? 0 }} 学分
          </span>
        </div>
        <div class="card-details">
          <div class="detail-row" v-if="course.teacher">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            {{ course.teacher }}
          </div>
          <div class="detail-row" v-if="course.location">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            {{ course.location }}
          </div>
          <div class="detail-row" v-if="course.timeSlots && course.timeSlots.length">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            {{ formatTimeSlots(course.timeSlots) }}
          </div>
        </div>
      </div>
    </div>

    <!-- No results -->
    <div v-if="!loading && courses.length > 0 && filteredCourses.length === 0" class="no-results">
      <p>没有找到匹配「{{ searchQuery }}」的课程</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getCourses, type Course } from '@/api/modules/courses'
import { DAYS_OF_WEEK } from '@/constants'
import { ElMessage } from 'element-plus'

const courses = ref<Course[]>([])
const loading = ref(false)
const searchQuery = ref('')

const filteredCourses = computed(() => {
  if (!searchQuery.value) return courses.value
  const q = searchQuery.value.toLowerCase()
  return courses.value.filter(c =>
    c.name?.toLowerCase().includes(q) ||
    c.teacher?.toLowerCase().includes(q) ||
    c.location?.toLowerCase().includes(q)
  )
})

const totalCredits = computed(() => filteredCourses.value.reduce((sum, c) => sum + (c.credits || 0), 0))
const uniqueTeachers = computed(() => new Set(filteredCourses.value.map(c => c.teacher).filter(Boolean)).size)

const formatTimeSlots = (slots: Course['timeSlots']) => {
  if (!slots || !slots.length) return '-'
  return slots.map(slot => {
    const dayLabel = DAYS_OF_WEEK.find(d => d.value === slot.dayOfWeek)?.label || slot.dayOfWeek
    return `${dayLabel} ${slot.start}-${slot.end}`
  }).join(', ')
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await getCourses()
    if (Array.isArray(res.data)) {
        courses.value = res.data
    } else if ((res.data as any).courses) {
        courses.value = (res.data as any).courses
    } else {
        courses.value = []
    }
  } catch (error) {
    ElMessage.error('加载课程失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchCourses() })
</script>

<style scoped>
/* ── Page Layout ────────────────────────────────── */
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
}

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

.btn-refresh {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  padding: var(--sp-2) var(--sp-4);
  background: var(--c-surface);
  color: var(--c-text-primary);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--duration-fast) ease;
}
.btn-refresh:hover:not(:disabled) {
  border-color: var(--c-primary);
  color: var(--c-primary);
  box-shadow: var(--shadow-sm);
}
.btn-refresh:disabled { opacity: 0.5; cursor: not-allowed; }

.spinner-icon {
  width: 16px;
  height: 16px;
  border: 2px solid var(--c-border);
  border-top-color: var(--c-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Search Bar ─────────────────────────────────── */
.search-bar {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  margin-bottom: var(--sp-4);
}
.search-bar svg {
  color: var(--c-text-muted);
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  color: var(--c-text-primary);
  outline: none;
}
.search-input::placeholder {
  color: var(--c-text-muted);
}
.search-clear {
  cursor: pointer;
  color: var(--c-text-muted);
  display: flex;
  padding: var(--sp-1);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) ease;
}
.search-clear:hover {
  background: var(--c-border-light);
  color: var(--c-text-primary);
}

/* ── Stats ──────────────────────────────────────── */
.stats-mini {
  display: flex;
  gap: var(--sp-3);
  margin-bottom: var(--sp-6);
}
.stat-pill {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
  padding: var(--sp-1) var(--sp-3);
  background: var(--c-primary-50);
  border-radius: var(--radius-full);
}
.stat-num {
  font-weight: 700;
  font-family: var(--font-mono);
  color: var(--c-primary);
  font-size: var(--text-sm);
}
.stat-txt {
  font-size: var(--text-xs);
  color: var(--c-text-muted);
}

/* ── Skeleton ───────────────────────────────────── */
.skeleton-area {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
}

/* ── Empty State ────────────────────────────────── */
.empty-state {
  text-align: center;
  padding: var(--sp-16) var(--sp-6);
  max-width: 420px;
  margin: var(--sp-8) auto;
}
.empty-state svg {
  color: var(--c-primary-200);
  margin-bottom: var(--sp-4);
}
.empty-state h3 {
  font-size: var(--text-lg);
  margin-bottom: var(--sp-2);
}
.empty-state p {
  color: var(--c-text-muted);
  font-size: var(--text-sm);
}

/* ── Data Table ─────────────────────────────────── */
.table-wrapper {
  padding: 0;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);
}

.data-table th {
  text-align: left;
  padding: var(--sp-3) var(--sp-4);
  font-weight: 600;
  color: var(--c-text-primary);
  background: var(--c-bg);
  border-bottom: 1px solid var(--c-border);
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-table td {
  padding: var(--sp-3) var(--sp-4);
  border-bottom: 1px solid var(--c-border-light);
  color: var(--c-text-secondary);
}

.table-row {
  transition: background var(--duration-fast) ease;
}
.table-row:hover {
  background: var(--c-primary-50);
}
.table-row:last-child td {
  border-bottom: none;
}

.cell-id {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--c-text-muted);
}

.cell-name {
  font-weight: 600;
  color: var(--c-text-primary);
}

.cell-credits {
  text-align: center;
}

.credit-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 700;
  font-family: var(--font-mono);
}
.credit-1 { background: #DBEAFE; color: #1E40AF; }
.credit-2 { background: #D1FAE5; color: #065F46; }
.credit-3 { background: #FEF3C7; color: #92400E; }
.credit-4 { background: #FFEDD5; color: #9A3412; }
.credit-5 { background: #FEE2E2; color: #991B1B; }

.cell-slots {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--c-text-muted);
}

/* ── Mobile Cards ───────────────────────────────── */
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
}

.course-card {
  padding: var(--sp-4);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-3);
}

.card-name {
  font-weight: 700;
  font-size: var(--text-sm);
  color: var(--c-text-primary);
}

.card-details {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
}

.detail-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  font-size: var(--text-xs);
  color: var(--c-text-muted);
}
.detail-row svg {
  flex-shrink: 0;
}

.no-results {
  text-align: center;
  padding: var(--sp-8);
  color: var(--c-text-muted);
  font-size: var(--text-sm);
}

/* ── Responsive Visibility ──────────────────────── */
.desktop-only { display: block; }
.mobile-only { display: none; }

@media (max-width: 767px) {
  .desktop-only { display: none; }
  .mobile-only { display: flex; }
  .page-header { flex-direction: column; }
  .stats-mini { flex-wrap: wrap; }
}
</style>
