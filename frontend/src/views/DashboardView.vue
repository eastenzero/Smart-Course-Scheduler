
<template>
  <div class="dashboard-view">
    <div class="header">
        <h1>Dashboard</h1>
        <div class="actions">
            <el-button @click="fetchSchedule" :disabled="isPolling">Refresh Status</el-button>
            <el-button type="primary" @click="startGeneration" :loading="isPolling">Generate Schedule</el-button>
        </div>
    </div>
    
    <div v-if="isPolling" class="polling-state">
        <el-progress :percentage="progress" status="success" :indeterminate="true" :duration="2" />
        <p class="polling-text">AI 正在计算最优方案...</p>
    </div>

    <div v-if="loading && !isPolling" class="state-info">Loading schedule...</div>
    <div v-else-if="!schedule && !isPolling" class="state-info">No schedule generated yet. Please generate a new one.</div>
    <div v-else-if="schedule">
        <div class="schedule-meta">
            <el-tag :type="getStatusType(schedule.status)">{{ schedule.status }}</el-tag>
            <span class="score" v-if="schedule.score">Score: Hard {{ schedule.score?.hard }}, Soft {{ schedule.score?.soft }}</span>
            <span class="error-msg" v-if="schedule.errorMessage">Error: {{ schedule.errorMessage }}</span>
        </div>
        <ScheduleGrid :entries="entries" class="grid-container" />
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

// Polling State
const isPolling = ref(false)
const progress = ref(0)
const pollingInterval = ref<any>(null)
const currentScheduleId = ref<string | null>(null)

const entries = computed(() => {
    return (schedule.value?.result?.entries || []) as ScheduleEntry[]
})

const getStatusType = (status: string) => {
    if (status === 'COMPLETED' || status === 'SOLVED') return 'success'
    if (status === 'RUNNING' || status === 'SOLVING') return 'warning'
    if (status === 'FAILED' || status === 'ERROR') return 'danger'
    return 'info'
}

const fetchSchedule = async (scheduleId?: string) => {
    loading.value = true
    console.log('fetchSchedule called with ID:', scheduleId)
    try {
        if (!scheduleId) {
            console.log('Fetching latest schedule (no ID provided)')
        }
        const res = await getScheduleResult(scheduleId) // Fetch latest or specific
        schedule.value = res.data.schedule
    } catch (e: any) {
        if (e.response && e.response.status === 404) {
            schedule.value = null // Valid empty state
        } else {
            console.error('Failed to fetch schedule', e)
        }
    } finally {
        loading.value = false
    }
}

const startGeneration = async () => {
    try {
        // 1. Trigger Generation
        isPolling.value = true
        progress.value = 0
        
        // Mock ID for now if backend fails, but let's try real call first
        // If backend isn't ready for this, we might need a try-catch fallback or mock mode switch
        // For now, assume we call real API.
        
        // TODO: Pass actual course IDs if needed, or backend uses all active courses by default
        const res = await triggerSchedule({ courseIds: [] }) 
        currentScheduleId.value = res.data.scheduleId
        
        // 2. Start Polling
        startPolling(currentScheduleId.value)
        
    } catch (e) {
        console.error('Failed to trigger generation', e)
        ElMessage.error('Failed to start schedule generation')
        isPolling.value = false
    }
}

const startPolling = (scheduleId: string) => {
    if (pollingInterval.value) clearInterval(pollingInterval.value)
    
    pollingInterval.value = setInterval(async () => {
        // Fake progress for UX
        if (progress.value < 90) progress.value += 5
        
        try {
            // 3. Check Status
            const res = await getScheduleStatus(scheduleId)
            const status = res.data.status
            
            if (status === 'SOLVED' || status === 'COMPLETED') {
                stopPolling()
                progress.value = 100
                ElMessage.success('Schedule Generated Successfully!')
                fetchSchedule(scheduleId) // Load result
            } else if (status === 'ERROR' || status === 'FAILED') {
                stopPolling()
                ElMessage.error('Scheduling Failed')
            }
            // If SOLVING/RUNNING/QUEUED, continue polling
            
        } catch (e) {
            console.error('Polling error', e)
            // Optional: stop polling on network error? Or retry?
        }
    }, 1500) // Poll every 1.5s
}

const stopPolling = () => {
    if (pollingInterval.value) clearInterval(pollingInterval.value)
    pollingInterval.value = null
    isPolling.value = false
}

onMounted(() => {
    fetchSchedule()
})

onUnmounted(() => {
    stopPolling()
})
</script>

<style scoped>
.dashboard-view {
  padding: 20px;
}
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.actions {
    display: flex;
    gap: 10px;
}
.state-info {
    text-align: center;
    color: #909399;
    padding: 40px;
}
.polling-state {
    padding: 20px;
    max-width: 500px;
    margin: 0 auto;
    text-align: center;
}
.polling-text {
    margin-top: 10px;
    color: #606266;
    font-size: 14px;
}
.schedule-meta {
    margin-bottom: 15px;
    display: flex;
    gap: 15px;
    align-items: center;
    flex-wrap: wrap;
}
.score {
    font-size: 14px;
    color: #606266;
}
.error-msg {
    color: #f56c6c;
    font-size: 14px;
}
.grid-container {
    margin-top: 10px;
}
</style>
