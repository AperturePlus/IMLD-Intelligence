<template>
  <header class="desktop-titlebar" :class="{ 'is-maximized': windowState.isMaximized }">
    <div class="titlebar-start drag-region">
      <span class="brand-indicator"></span>
      <span class="brand-name">{{ BRANDING.workspaceName }}</span>
    </div>

    <div class="titlebar-center drag-region" @dblclick="toggleMaximize">
      <span class="context-label">{{ currentPageTitle }}</span>
    </div>

    <div class="titlebar-end no-drag">
      <button
        v-if="showHomeShortcut"
        type="button"
        class="home-btn"
        :class="{ 'is-active': route.path === homePath }"
        @click="goHome"
      >
        <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
          <path d="M2 6L8 2L14 6V13C14 13.5523 13.5523 14 13 14H3C2.44772 14 2 13.5523 2 13V6Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
          <path d="M6 14V9H10V14" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
        </svg>
        <span>首页</span>
      </button>

      <div class="window-controls">
        <button
          type="button"
          class="wc-btn"
          aria-label="最小化"
          @click="minimizeWindow"
        >
          <svg width="12" height="12" viewBox="0 0 12 12"><line x1="2" y1="6" x2="10" y2="6" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
        </button>
        <button
          type="button"
          class="wc-btn"
          :aria-label="windowState.isMaximized ? '还原窗口' : '最大化窗口'"
          @click="toggleMaximize"
        >
          <svg v-if="!windowState.isMaximized" width="12" height="12" viewBox="0 0 12 12">
            <rect x="2" y="2" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.3" fill="none"/>
          </svg>
          <svg v-else width="12" height="12" viewBox="0 0 12 12">
            <rect x="3.5" y="1" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.2" fill="none"/>
            <rect x="1.5" y="3.5" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.2" fill="var(--electron-titlebar-bg, #fff)"/>
          </svg>
        </button>
        <button
          type="button"
          class="wc-btn wc-btn--close"
          aria-label="关闭窗口"
          @click="closeWindow"
        >
          <svg width="12" height="12" viewBox="0 0 12 12">
            <line x1="2.5" y1="2.5" x2="9.5" y2="9.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
            <line x1="9.5" y1="2.5" x2="2.5" y2="9.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BRANDING } from '@/constants/branding'

const route = useRoute()
const router = useRouter()
const shell = window.electron?.shell
const homePath = '/center/welcome'
const windowState = ref<ElectronWindowState>({
  isMaximized: false,
  isFullscreen: false
})

const currentPageTitle = computed(() => route.meta.title ?? '首页')
const showHomeShortcut = computed(() => route.path.startsWith('/center'))

const syncWindowState = async () => {
  const nextState = await shell?.getWindowState()
  if (nextState) {
    windowState.value = nextState
  }
}

const goHome = () => {
  if (route.path !== homePath) {
    router.push(homePath)
  }
}

const minimizeWindow = () => {
  shell?.minimize()
}

const toggleMaximize = () => {
  shell?.toggleMaximize()
}

const closeWindow = () => {
  shell?.close()
}

let removeWindowStateListener: (() => void) | undefined

onMounted(async () => {
  await syncWindowState()
  removeWindowStateListener = shell?.onWindowStateChange((nextState) => {
    windowState.value = nextState
  })
})

onBeforeUnmount(() => {
  removeWindowStateListener?.()
  document.documentElement.classList.remove('electron-shell-maximized')
})

watch(
  () => windowState.value.isMaximized,
  (isMaximized) => {
    document.documentElement.classList.toggle('electron-shell-maximized', isMaximized)
  },
  { immediate: true }
)
</script>

<style scoped>
.desktop-titlebar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 2000;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 0;
  height: var(--electron-titlebar-safe-top);
  padding: 0;
  background: var(--electron-titlebar-bg);
  border-bottom: 1px solid var(--electron-titlebar-border);
  box-shadow: var(--electron-titlebar-shadow);
  backdrop-filter: blur(20px) saturate(1.6);
  user-select: none;
  -webkit-font-smoothing: antialiased;
}

.desktop-titlebar.is-maximized {
  box-shadow: none;
}

.drag-region {
  -webkit-app-region: drag;
}

.no-drag {
  -webkit-app-region: no-drag;
}

.titlebar-start {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 8px 0 14px;
  height: 100%;
}

.brand-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--brand-primary), var(--brand-secondary));
  box-shadow: 0 0 6px rgba(15, 109, 141, 0.35);
  flex-shrink: 0;
}

.brand-name {
  font-size: 12px;
  font-weight: 600;
  color: #1e293b;
  letter-spacing: 0.02em;
  white-space: nowrap;
  opacity: 0.85;
}

.titlebar-center {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 0 12px;
  overflow: hidden;
}

.context-label {
  font-size: 12px;
  font-weight: 500;
  color: #475569;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.01em;
}

.titlebar-end {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 6px 0 0;
  height: 100%;
}

.home-btn {
  height: 26px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0 10px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 6px;
  background: transparent;
  color: #64748b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.home-btn:hover {
  background: rgba(15, 109, 141, 0.06);
  color: var(--brand-primary);
  border-color: rgba(15, 109, 141, 0.2);
}

.home-btn.is-active {
  background: rgba(15, 109, 141, 0.1);
  color: var(--brand-primary);
  border-color: rgba(15, 109, 141, 0.24);
}

.window-controls {
  display: inline-flex;
  align-items: center;
  height: 100%;
  gap: 0;
}

.wc-btn {
  position: relative;
  width: 40px;
  height: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.12s ease;
}

.wc-btn:hover {
  background: rgba(15, 23, 42, 0.05);
  color: #334155;
}

.wc-btn:active {
  background: rgba(15, 23, 42, 0.08);
}

.wc-btn--close:hover {
  background: #ef4444;
  color: #ffffff;
}

.wc-btn--close:active {
  background: #dc2626;
}

@media (max-width: 960px) {
  .brand-name {
    display: none;
  }
}

@media (max-width: 720px) {
  .brand-name,
  .home-btn {
    display: none;
  }

  .wc-btn {
    width: 36px;
  }
}
</style>

