
<template>
  <div class="setup-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">排课偏好设置</h1>
        <p class="page-desc">配置排课约束和偏好，AI 将在生成课表时优先满足高权重项。</p>
      </div>
      <button class="btn-primary" @click="save" :disabled="saving">
        <svg v-if="!saving" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
          <polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>
        </svg>
        <span class="spinner-sm" v-else />
        {{ saving ? '保存中...' : '保存配置' }}
      </button>
    </div>

    <!-- Two-Column Grid -->
    <div class="setup-grid">
      <!-- Left: Add Constraint -->
      <section class="form-card surface-card">
        <div class="card-header">
          <h2 class="card-title">添加约束</h2>
        </div>

        <div class="card-body">
          <el-form :model="newConstraint" label-position="top">
            <el-form-item label="约束类型">
              <el-select
                v-model="newConstraint.constraintKey"
                placeholder="请选择约束类型"
                style="width: 100%"
                @change="handleTypeChange"
              >
                <el-option
                  v-for="(label, key) in ConstraintLabels"
                  :key="key"
                  :label="label"
                  :value="key"
                />
              </el-select>
            </el-form-item>

            <template v-if="newConstraint.constraintKey">
              <el-form-item label="权重 (1-10)">
                <el-slider v-model="newConstraint.weight" :min="1" :max="10" show-input style="padding-right: 8px" />
                <p class="field-hint">权重越高，排课时越优先满足</p>
              </el-form-item>

              <!-- Time params -->
              <template v-if="newConstraint.params && getParamType(newConstraint.constraintKey) === 'time'">
                <el-form-item :label="getParamLabel(newConstraint.constraintKey)">
                  <el-time-select
                    v-model="newConstraint.params[getParamKey(newConstraint.constraintKey)]"
                    start="08:00" step="00:15" end="22:00"
                    placeholder="选择时间"
                    style="width: 100%"
                  />
                </el-form-item>
              </template>

              <!-- Number params -->
              <template v-else-if="newConstraint.params && getParamType(newConstraint.constraintKey) === 'number'">
                <el-form-item :label="getParamLabel(newConstraint.constraintKey)">
                  <el-input-number v-model="newConstraint.params[getParamKey(newConstraint.constraintKey)]" :min="1" />
                </el-form-item>
              </template>

              <!-- Lunch params -->
              <template v-else-if="newConstraint.params && newConstraint.constraintKey === ConstraintType.PREFER_FREE_LUNCH">
                <el-form-item label="午休窗口开始">
                  <el-time-select v-model="newConstraint.params.windowStart" start="11:00" step="00:10" end="14:00" style="width:100%" />
                </el-form-item>
                <el-form-item label="午休窗口结束">
                  <el-time-select v-model="newConstraint.params.windowEnd" start="11:30" step="00:10" end="14:30" style="width:100%" />
                </el-form-item>
                <el-form-item label="最少空闲分钟">
                  <el-input-number v-model="newConstraint.params.minFreeMinutes" :min="30" :step="10" />
                </el-form-item>
              </template>

              <!-- No Friday info -->
              <template v-else-if="newConstraint.constraintKey === ConstraintType.NO_FRIDAY">
                <div class="info-box">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
                  </svg>
                  <span>将尽量避免在周五安排课程</span>
                </div>
              </template>

              <button class="btn-add" @click="addConstraint">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                添加此约束
              </button>
            </template>
          </el-form>
        </div>
      </section>

      <!-- Right: Configured Constraints -->
      <section class="list-section">
        <div class="section-header">
          <h2 class="section-title">已配置的约束</h2>
          <span class="count-tag" v-if="constraints.length > 0">{{ constraints.length }}</span>
        </div>

        <!-- Empty State -->
        <div v-if="constraints.length === 0" class="empty-state surface-card">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <line x1="12" y1="8" x2="12" y2="16"/>
            <line x1="8" y1="12" x2="16" y2="12"/>
          </svg>
          <h3>尚未添加约束</h3>
          <p>在左侧选择约束类型并配置参数，然后点击「添加此约束」。</p>
        </div>

        <!-- Constraint Items -->
        <TransitionGroup name="list" tag="div" class="constraint-list">
          <div
            v-for="(c, index) in constraints"
            :key="c.constraintKey + '-' + index"
            class="constraint-item surface-card"
          >
            <div class="constraint-main">
              <div class="weight-badge">{{ c.weight }}</div>
              <div class="constraint-detail">
                <div class="constraint-name">{{ ConstraintLabels[c.constraintKey as ConstraintType] }}</div>
                <div class="constraint-params" v-if="c.params && Object.keys(c.params).length">
                  <span v-for="(val, key) in c.params" :key="key" class="param-tag">
                    {{ key }}: {{ val }}
                  </span>
                </div>
              </div>
            </div>
            <button class="btn-delete" @click="removeConstraint(index)" aria-label="删除">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
            </button>
          </div>
        </TransitionGroup>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ConstraintType, ConstraintLabels } from '@/constants'
import { saveConstraints, type Constraint } from '@/api/modules/constraints'
import { ElMessage } from 'element-plus'

const saving = ref(false)
const constraints = ref<Constraint[]>([])

const newConstraint = reactive<Constraint>({
    constraintKey: '',
    weight: 5,
    enabled: true,
    params: {}
})

const handleTypeChange = (val: string) => {
    newConstraint.params = {}
    if (val === ConstraintType.NO_FRIDAY) {
        newConstraint.params = { bannedDayOfWeek: 5 }
    }
}

const getParamType = (key: string) => {
    if ([
        ConstraintType.NO_EARLY_CLASS,
        ConstraintType.AVOID_LATE_END,
        ConstraintType.PREFER_MORNING,
        ConstraintType.PREFER_AFTERNOON
    ].includes(key as any)) return 'time'
    if ([
        ConstraintType.MINIMIZE_GAPS,
        ConstraintType.PREFER_COMPACT_DAYS,
        ConstraintType.MAXIMIZE_DAYS_OFF,
        ConstraintType.AVOID_LONG_CONTINUOUS
    ].includes(key as any)) return 'number'
    return 'custom'
}

const getParamKey = (key: string) => {
    switch(key) {
        case ConstraintType.NO_EARLY_CLASS: return 'earliestStart'
        case ConstraintType.AVOID_LATE_END: return 'latestEnd'
        case ConstraintType.PREFER_MORNING: return 'morningEnd'
        case ConstraintType.PREFER_AFTERNOON: return 'afternoonStart'
        case ConstraintType.MINIMIZE_GAPS: return 'maxGapMinutes'
        case ConstraintType.PREFER_COMPACT_DAYS: return 'maxGapMinutes'
        case ConstraintType.MAXIMIZE_DAYS_OFF: return 'minDaysOff'
        case ConstraintType.AVOID_LONG_CONTINUOUS: return 'maxContinuousMinutes'
        default: return 'value'
    }
}

const getParamLabel = (key: string) => {
    switch(key) {
        case ConstraintType.NO_EARLY_CLASS: return '最早开始时间'
        case ConstraintType.AVOID_LATE_END: return '最晚结束时间'
        case ConstraintType.PREFER_MORNING: return '上午结束时间'
        case ConstraintType.PREFER_AFTERNOON: return '下午开始时间'
        case ConstraintType.MINIMIZE_GAPS: return '最大空闲分钟'
        case ConstraintType.PREFER_COMPACT_DAYS: return '最大空闲分钟'
        case ConstraintType.MAXIMIZE_DAYS_OFF: return '最少空闲天数'
        case ConstraintType.AVOID_LONG_CONTINUOUS: return '最大连续分钟'
        default: return '参数'
    }
}

const addConstraint = () => {
    if (!newConstraint.constraintKey) return
    constraints.value.push({
        ...newConstraint,
        params: { ...newConstraint.params }
    })
    newConstraint.weight = 5
    newConstraint.params = {}
    if (newConstraint.constraintKey) {
       handleTypeChange(newConstraint.constraintKey)
    }
}

const removeConstraint = (index: number) => {
    constraints.value.splice(index, 1)
}

const save = async () => {
    saving.value = true
    try {
        await saveConstraints({ constraints: constraints.value })
        ElMessage.success('保存成功')
    } catch (e) {
        ElMessage.error('保存失败')
        console.error(e)
    } finally {
        saving.value = false
    }
}
</script>

<style scoped>
.setup-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* ── Page Header ────────────────────────────────── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--sp-8);
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
  line-height: 1.6;
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
  flex-shrink: 0;
  transition: background var(--duration-fast) ease;
}
.btn-primary:hover:not(:disabled) {
  background: var(--c-primary-dark);
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner-sm {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Grid ───────────────────────────────────────── */
.setup-grid {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: var(--sp-6);
  align-items: start;
}

/* ── Form Card ──────────────────────────────────── */
.form-card {
  position: sticky;
  top: var(--sp-4);
}

.card-header {
  padding: var(--sp-4) var(--sp-5);
  border-bottom: 1px solid var(--c-border);
}

.card-title {
  font-size: var(--text-base);
  font-weight: 600;
}

.card-body {
  padding: var(--sp-5);
}

.field-hint {
  font-size: var(--text-xs);
  color: var(--c-text-muted);
  margin-top: var(--sp-1);
}

.info-box {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  padding: var(--sp-3) var(--sp-4);
  background: var(--c-primary-50);
  color: var(--c-primary-dark);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  margin-bottom: var(--sp-4);
}

.btn-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--sp-2);
  width: 100%;
  padding: 10px;
  background: var(--c-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  margin-top: var(--sp-4);
  transition: background var(--duration-fast) ease;
}
.btn-add:hover {
  background: var(--c-primary-dark);
}

/* ── List Section ───────────────────────────────── */
.section-header {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
}

.section-title {
  font-size: var(--text-lg);
}

.count-tag {
  background: var(--c-primary-50);
  color: var(--c-primary);
  font-size: var(--text-xs);
  font-weight: 700;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-family: var(--font-mono);
}

/* ── Empty State ────────────────────────────────── */
.empty-state {
  text-align: center;
  padding: var(--sp-10) var(--sp-6);
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
  max-width: 280px;
  margin: 0 auto;
  line-height: 1.6;
}

/* ── Constraint Item ────────────────────────────── */
.constraint-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
}

.constraint-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-4);
}

.constraint-main {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  min-width: 0;
}

.weight-badge {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: var(--c-primary-50);
  color: var(--c-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: 800;
  font-family: var(--font-mono);
  flex-shrink: 0;
}

.constraint-detail {
  min-width: 0;
}

.constraint-name {
  font-weight: 600;
  font-size: var(--text-sm);
  color: var(--c-text-primary);
  margin-bottom: 2px;
}

.constraint-params {
  display: flex;
  gap: var(--sp-2);
  flex-wrap: wrap;
}

.param-tag {
  display: inline-block;
  background: var(--c-border-light);
  color: var(--c-text-secondary);
  padding: 1px 8px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-family: var(--font-mono);
}

.btn-delete {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--c-text-muted);
  cursor: pointer;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
  transition: all var(--duration-fast) ease;
}
.btn-delete:hover {
  background: var(--c-danger-light);
  color: var(--c-danger);
}

/* ── Responsive ─────────────────────────────────── */
@media (max-width: 900px) {
  .setup-grid {
    grid-template-columns: 1fr;
  }
  .form-card {
    position: static;
  }
}

@media (max-width: 480px) {
  .page-header {
    flex-direction: column;
  }
  .btn-primary {
    width: 100%;
    justify-content: center;
  }
}
</style>
