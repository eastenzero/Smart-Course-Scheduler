
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import SetupView from '@/views/SetupView.vue'
import AdminView from '@/views/AdminView.vue'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'Dashboard',
        component: DashboardView
    },
    {
        path: '/setup',
        name: 'Setup',
        component: SetupView
    },
    {
        path: '/admin',
        name: 'Admin',
        component: AdminView
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
