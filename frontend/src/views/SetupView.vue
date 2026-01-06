
<template>
  <div class="setup-view">
    <div class="header">
        <h1>排课偏好设置</h1>
        <div class="actions">
            <el-button type="primary" @click="save" :loading="saving">保存配置</el-button>
        </div>
    </div>

    <el-row :gutter="20">
        <el-col :span="8">
            <el-card class="add-card">
                <template #header>
                    <div class="card-header">
                        <span>添加约束</span>
                    </div>
                </template>
                <el-form :model="newConstraint" label-position="top">
                    <el-form-item label="约束类型">
                        <el-select v-model="newConstraint.constraintKey" placeholder="选择类型" style="width: 100%" @change="handleTypeChange">
                            <el-option
                                v-for="(label, key) in ConstraintLabels"
                                :key="key"
                                :label="label"
                                :value="key"
                            />
                        </el-select>
                    </el-form-item>
                    
                    <div v-if="newConstraint.constraintKey">
                        <el-form-item label="权重 (1-10)">
                            <div class="slider-container">
                                <el-slider v-model="newConstraint.weight" :min="1" :max="10" show-input />
                            </div>
                            <div class="help-text">权重越高，排课时越优先满足</div>
                        </el-form-item>

                        <!-- Dynamic Params -->
                        <template v-if="newConstraint.params && getParamType(newConstraint.constraintKey) === 'time'">
                            <el-form-item :label="getParamLabel(newConstraint.constraintKey)">
                                <el-time-select
                                    v-model="newConstraint.params[getParamKey(newConstraint.constraintKey)]"
                                    start="08:00"
                                    step="00:15"
                                    end="22:00"
                                    placeholder="选择时间"
                                    style="width: 100%"
                                />
                            </el-form-item>
                        </template>

                        <template v-else-if="newConstraint.params && getParamType(newConstraint.constraintKey) === 'number'">
                            <el-form-item :label="getParamLabel(newConstraint.constraintKey)">
                                <el-input-number v-model="newConstraint.params[getParamKey(newConstraint.constraintKey)]" :min="1" />
                            </el-form-item>
                        </template>

                        <template v-else-if="newConstraint.params && newConstraint.constraintKey === ConstraintType.PREFER_FREE_LUNCH">
                            <el-form-item label="午休窗口开始">
                                <el-time-select v-model="newConstraint.params.windowStart" start="11:00" step="00:10" end="14:00" />
                            </el-form-item>
                            <el-form-item label="午休窗口结束">
                                <el-time-select v-model="newConstraint.params.windowEnd" start="11:30" step="00:10" end="14:30" />
                            </el-form-item>
                             <el-form-item label="最少空闲分钟">
                                <el-input-number v-model="newConstraint.params.minFreeMinutes" :min="30" :step="10" />
                            </el-form-item>
                        </template>

                        <template v-else-if="newConstraint.constraintKey === ConstraintType.NO_FRIDAY">
                            <el-alert title="将尽量避免在周五安排课程" type="info" :closable="false" />
                        </template>

                         <el-button type="success" @click="addConstraint" style="width: 100%; margin-top: 20px">添加</el-button>
                    </div>
                </el-form>
            </el-card>
        </el-col>
        
        <el-col :span="16">
            <div class="constraints-list">
                <h3>已配置的约束 ({{ constraints.length }})</h3>
                <el-empty v-if="constraints.length === 0" description="暂无约束，请在左侧添加" />
                
                <el-card v-for="(c, index) in constraints" :key="index" class="constraint-item" shadow="hover">
                    <div class="constraint-row">
                        <div class="constraint-info">
                            <div class="constraint-title">
                                <el-tag size="small" effect="dark">{{ c.weight }}</el-tag>
                                <span class="label">{{ ConstraintLabels[c.constraintKey as ConstraintType] }}</span>
                            </div>
                            <div class="constraint-params" v-if="c.params && Object.keys(c.params).length">
                                <span v-for="(val, key) in c.params" :key="key" class="param-tag">
                                    {{ key }}: {{ val }}
                                </span>
                            </div>
                        </div>
                        <el-button type="danger" circle link @click="removeConstraint(index)">
                             <el-icon><Delete /></el-icon>
                        </el-button>
                    </div>
                </el-card>
            </div>
        </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ConstraintType, ConstraintLabels } from '@/constants'
import { saveConstraints, type Constraint } from '@/api/modules/constraints'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const saving = ref(false)
const constraints = ref<Constraint[]>([])

const newConstraint = reactive<Constraint>({
    constraintKey: '',
    weight: 5,
    enabled: true,
    params: {}
})

// Validation or default params setup
const handleTypeChange = (val: string) => {
    newConstraint.params = {}
    if (val === ConstraintType.NO_FRIDAY) {
        newConstraint.params = { bannedDayOfWeek: 5 }
    }
}

// Helpers for dynamic form rendering
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
    
    // Deep copy params to avoid ref issues
    const constraintToAdd = {
        ...newConstraint,
        params: { ...newConstraint.params }
    }
    
    constraints.value.push(constraintToAdd)
    
    // Reset form but keep defaults
    newConstraint.weight = 5
    // newConstraint.constraintKey = '' // Option: keep selected or reset? Reset usually better
    newConstraint.params = {} 
    
    // If kept key, need to re-init default params if any
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
.setup-view {
  padding: 20px;
}
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.help-text {
    font-size: 12px;
    color: #909399;
    line-height: 1.2;
    margin-top: 5px;
}
.constraint-item {
    margin-bottom: 10px;
}
.constraint-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.constraint-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: bold;
    margin-bottom: 5px;
}
.constraint-params {
    font-size: 12px;
    color: #606266;
    display: flex;
    gap: 10px;
}
.param-tag {
    background: #f4f4f5;
    padding: 2px 6px;
    border-radius: 4px;
}
</style>
