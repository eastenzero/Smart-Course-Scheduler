
import client from '../client'

export interface ScheduleOptions {
    maxSeconds?: number
}

export interface TriggerScheduleRequest {
    courseIds: string[]
    options?: ScheduleOptions
}

export interface TriggerScheduleResponse {
    scheduleId: string
    status: 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'SOLVING'
}

export interface ScheduleStatusResponse {
    scheduleId: string
    status: 'SOLVING' | 'SOLVED' | 'ERROR' | 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED'
}

export interface ScheduleResultResponse {
    schedule: {
        id: string
        status: string
        result?: {
            entries: any[]
        }
        score?: {
            hard: number
            soft: number
        }
        errorMessage?: string | null
    }
}

export const triggerSchedule = (data: TriggerScheduleRequest) => {
    return client.post<TriggerScheduleResponse>('/schedule/generate', data)
}

export const getScheduleStatus = (scheduleId: string) => {
    return client.get<ScheduleStatusResponse>('/schedule/status', { params: { scheduleId } })
}

export const getScheduleResult = (scheduleId?: string) => {
    if (scheduleId) {
        return client.get<ScheduleResultResponse>('/schedule/result', { params: { scheduleId } })
    }
    // If no ID, try fetching latest (backend behavior) or just call without params
    return client.get<ScheduleResultResponse>('/schedule/result')
}
