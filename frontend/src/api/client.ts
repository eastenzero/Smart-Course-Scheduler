
import axios from 'axios'
// import { setupMock } from './mock'

const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json; charset=utf-8'
    }
})

// Enable mock for now
// setupMock(client)

export default client
