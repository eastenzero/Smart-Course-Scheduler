
import type { AxiosInstance } from 'axios'

export function setupMock(client: AxiosInstance) {
    // Simple mock adapter using interceptors
    client.interceptors.request.use(async (config) => {
        console.log(`[Mock] ${config.method?.toUpperCase()} ${config.url}`, config.data)

        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 500))

        // Match routes
        if (config.url === '/constraints' && config.method === 'post') {
            const data = config.data
            // Return structured response
            config.adapter = async () => {
                return {
                    data: {
                        constraints: data.constraints.map((c: any) => ({
                            ...c,
                            id: 'mock-uuid-' + Math.random().toString(36).substr(2, 9)
                        }))
                    },
                    status: 200,
                    statusText: 'OK',
                    headers: {},
                    config
                }
            }
        }

        if (config.url === '/schedule/generate' && config.method === 'post') {
            config.adapter = async () => {
                return {
                    data: {
                        scheduleId: 'mock-schedule-id-123',
                        status: 'QUEUED'
                    },
                    status: 202,
                    statusText: 'Accepted',
                    headers: {},
                    config
                }
            }
        }

        if (config.url === '/schedule/result' && config.method === 'get') {
            config.adapter = async () => {
                return {
                    data: {
                        schedule: {
                            id: 'mock-schedule-id-123',
                            status: 'COMPLETED',
                            requestedAt: new Date().toISOString(),
                            startedAt: new Date().toISOString(),
                            finishedAt: new Date().toISOString(),
                            score: { hard: 0, soft: -10 },
                            result: {
                                entries: [
                                    {
                                        courseId: 'c1',
                                        courseName: 'System Design',
                                        dayOfWeek: 1, // Mon
                                        start: '08:00', // SLOT_1
                                        end: '09:40',   // End of SLOT_2
                                        location: 'Room 303',
                                        teacher: 'Prof. Alice'
                                    },
                                    {
                                        courseId: 'c2',
                                        courseName: 'Vue 3 Advanced',
                                        dayOfWeek: 1, // Mon
                                        start: '10:00', // SLOT_3
                                        end: '11:40',   // End of SLOT_4
                                        location: 'Lab 2',
                                        teacher: 'Prof. Bob'
                                    },
                                    {
                                        courseId: 'c3',
                                        courseName: 'Data Structures',
                                        dayOfWeek: 2, // Tue
                                        start: '13:00', // SLOT_5
                                        end: '14:40',   // End of SLOT_6
                                        location: 'Room 101',
                                        teacher: 'Prof. Charlie'
                                    },
                                    {
                                        courseId: 'c4',
                                        courseName: 'Algorithms',
                                        dayOfWeek: 3, // Wed
                                        start: '10:00', // SLOT_3
                                        end: '11:40',   // End of SLOT_4
                                        location: 'Room 101',
                                        teacher: 'Prof. Charlie'
                                    },
                                    {
                                        courseId: 'c5',
                                        courseName: 'Database Systems',
                                        dayOfWeek: 5, // Fri
                                        start: '08:55', // SLOT_2
                                        end: '11:40',   // End of SLOT_4 (3 slots: 2,3,4? Wait, 08:55-09:40(2), 10:00-10:45(3), 10:55-11:40(4). Yes, spans 3 slots)
                                        location: 'Room 205',
                                        teacher: 'Prof. Dave'
                                    }
                                ]
                            },
                            errorMessage: null
                        }
                    },
                    status: 200,
                    statusText: 'OK',
                    headers: {},
                    config
                }
            }
        }

        return config
    })
}
