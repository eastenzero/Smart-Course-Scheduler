
<template>
  <div class="admin-view">
    <div class="header">
      <h1>Courses Management</h1>
      <el-button type="primary" @click="fetchCourses" :loading="loading">Refresh</el-button>
    </div>

    <el-table :data="courses" style="width: 100%" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="100" show-overflow-tooltip />
      <el-table-column prop="name" label="Course Name" min-width="150" />
      <el-table-column prop="teacher" label="Teacher" width="120" />
      <el-table-column prop="credits" label="Credits" width="80" align="center" />
      <el-table-column prop="location" label="Location" width="120" />
      
      <el-table-column label="Time Slots" min-width="200">
        <template #default="scope">
          {{ formatTimeSlots(scope.row.timeSlots) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCourses, type Course } from '@/api/modules/courses'
import { DAYS_OF_WEEK } from '@/constants'
import { ElMessage } from 'element-plus'

const courses = ref<Course[]>([])
const loading = ref(false)

const formatTimeSlots = (slots: Course['timeSlots']) => {
  if (!slots || !slots.length) return '-'
  
  return slots.map(slot => {
    const dayLabel = DAYS_OF_WEEK.find(d => d.value === slot.dayOfWeek)?.label || slot.dayOfWeek
    // Simple format: Mon 08:00-09:40
    return `${dayLabel} ${slot.start}-${slot.end}`
  }).join(', ')
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await getCourses()
    // Axios response data is usually the body. 
    // If backend returns array directly: res.data
    // If backend returns { courses: [...] }: res.data.courses
    // Prompt implies 20+ seed data. Let's assume array based on common REST patterns or check devtools later.
    // Spec doesn't strictly define GET /courses response structure, but usually lists are arrays.
    // Wait, prompt 2.2 mentions "JSON data" for TimeSlots.
    
    // Safety check for response structure
    if (Array.isArray(res.data)) {
        courses.value = res.data
    } else if ((res.data as any).courses) {
        courses.value = (res.data as any).courses
    } else {
        courses.value = []
        console.warn('Unexpected response format', res.data)
    }
  } catch (error) {
    ElMessage.error('Failed to load courses')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.admin-view {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
