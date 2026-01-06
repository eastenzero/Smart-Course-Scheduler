
import client from '../client'

export interface Course {
    id: string
    name: string
    teacher?: string
    credits?: number
    location?: string
    // raw TimeSlots from backend might be object or string, assuming strict array of objects based on usage context, 
    // but the prompt mentions "JSON data" to stringify. 
    // Usually backend returns JSON array for @ElementCollection or simple JSONB.
    // Let's assume it's an array of objects we need to format.
    timeSlots?: Array<{
        dayOfWeek: number
        start: string // Backend uses "start" not "startTime"
        end: string   // Backend uses "end" not "endTime"
    }>
}

export interface GetCoursesResponse {
    courses: Course[]
}

export const getCourses = () => {
    return client.get<Course[]>('/courses') // Verify endpoint: prompt says "GET /api/courses"
}
