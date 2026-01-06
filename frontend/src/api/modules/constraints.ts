
import client from '../client'

export interface Constraint {
    constraintKey: string
    weight: number
    enabled: boolean
    params?: Record<string, any>
    id?: string
}

export interface SaveConstraintsRequest {
    constraints: Constraint[]
}

export interface SaveConstraintsResponse {
    constraints: Constraint[]
}

export const saveConstraints = (data: SaveConstraintsRequest) => {
    return client.post<SaveConstraintsResponse>('/constraints', data)
}
