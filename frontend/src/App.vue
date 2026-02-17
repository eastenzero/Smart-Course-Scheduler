
<template>
  <div class="app-layout" :class="{ 'sidebar-open': sidebarOpen }">
    <!-- Mobile Overlay -->
    <div
      v-if="sidebarOpen && isMobile"
      class="sidebar-overlay"
      @click="sidebarOpen = false"
    />

    <!-- Sidebar -->
    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-header">
        <div class="logo-group" @click="$router.push('/')">
          <div class="logo-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
              <path d="M8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01"/>
            </svg>
          </div>
          <div class="logo-text">
            <span class="brand-name">Smart Scheduler</span>
            <span class="brand-sub">智能排课系统</span>
          </div>
        </div>
        <button v-if="isMobile" class="close-btn" @click="sidebarOpen = false" aria-label="关闭菜单">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="isMobile && (sidebarOpen = false)"
        >
          <span class="nav-icon" v-html="item.icon" />
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <span class="version-tag">v1.0.0</span>
      </div>
    </aside>

    <!-- Main -->
    <div class="main-wrapper">
      <header v-if="isMobile" class="mobile-header">
        <button class="hamburger-btn" @click="sidebarOpen = true" aria-label="打开菜单">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </button>
        <span class="mobile-title">Smart Scheduler</span>
        <div style="width:22px" />
      </header>

      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const sidebarOpen = ref(false)
const windowWidth = ref(window.innerWidth)

const isMobile = computed(() => windowWidth.value < 768)

const navItems: Array<{ path: string; label: string; icon: string }> = [
  {
    path: '/',
    label: 'Dashboard',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>'
  },
  {
    path: '/setup',
    label: '排课偏好',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>'
  },
  {
    path: '/admin',
    label: '课程管理',
    icon: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>'
  }
]

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const handleResize = () => {
  windowWidth.value = window.innerWidth
  if (!isMobile.value) sidebarOpen.value = false
}

onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

watch(() => route.path, () => {
  if (isMobile.value) sidebarOpen.value = false
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: var(--c-bg);
}

/* ── Sidebar ────────────────────────────────────── */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: var(--sidebar-width);
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F9FAFB;
  border-right: 1px solid #E5E7EB;
  box-shadow: 1px 0 4px rgba(0, 0, 0, 0.04);
  z-index: var(--z-sidebar);
  transition: transform var(--duration-normal) var(--ease-out);
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-5) var(--sp-4);
  border-bottom: 1px solid var(--c-border);
}

.logo-group {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  cursor: pointer;
  user-select: none;
}

.logo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--c-primary);
  border-radius: var(--radius-sm);
  color: white;
  flex-shrink: 0;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.brand-name {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--c-text-primary);
}

.brand-sub {
  font-size: var(--text-xs);
  color: var(--c-text-muted);
}

.close-btn {
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
}
.close-btn:hover {
  background: var(--c-border-light);
  color: var(--c-text-primary);
}

/* ── Nav Items ──────────────────────────────────── */
.sidebar-nav {
  flex: 1;
  padding: var(--sp-3) var(--sp-3);
  display: flex;
  flex-direction: column;
  gap: var(--sp-1);
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  color: var(--c-text-secondary);
  font-size: var(--text-sm);
  font-weight: 500;
  text-decoration: none;
  transition: background var(--duration-fast) ease, color var(--duration-fast) ease;
  cursor: pointer;
}

.nav-item:hover {
  background: var(--c-border-light);
  color: var(--c-text-primary);
}

.nav-item.active {
  background: var(--c-primary-50);
  color: var(--c-primary);
  font-weight: 600;
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.nav-label {
  white-space: nowrap;
}

/* ── Sidebar Footer ─────────────────────────────── */
.sidebar-footer {
  padding: var(--sp-3) var(--sp-4);
  border-top: 1px solid var(--c-border);
}

.version-tag {
  font-size: var(--text-xs);
  color: var(--c-text-muted);
  font-family: var(--font-mono);
}

/* ── Main Content ───────────────────────────────── */
.main-wrapper {
  flex: 1;
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  padding: var(--sp-8);
  max-width: var(--content-max-width);
  width: 100%;
  margin: 0 auto;
}

/* ── Mobile Header ──────────────────────────────── */
.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-3) var(--sp-4);
  background: #FFFFFF;
  border-bottom: 1px solid var(--c-border);
  position: sticky;
  top: 0;
  z-index: calc(var(--z-sidebar) - 1);
}

.hamburger-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border: none;
  background: transparent;
  color: var(--c-text-primary);
  cursor: pointer;
  border-radius: var(--radius-sm);
}
.hamburger-btn:hover {
  background: var(--c-border-light);
}

.mobile-title {
  font-size: var(--text-base);
  font-weight: 700;
  color: var(--c-text-primary);
}

/* ── Mobile Overlay ─────────────────────────────── */
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.25);
  z-index: calc(var(--z-sidebar) - 1);
}

/* ── Responsive ─────────────────────────────────── */
@media (max-width: 767px) {
  .sidebar {
    transform: translateX(-100%);
  }
  .sidebar.open {
    transform: translateX(0);
    box-shadow: var(--shadow-lg);
  }
  .main-wrapper {
    margin-left: 0;
  }
  .main-content {
    padding: var(--sp-4);
  }
}

@media (min-width: 768px) {
  .mobile-header {
    display: none;
  }
  .sidebar-overlay {
    display: none;
  }
}
</style>
