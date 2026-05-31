<template>
  <el-aside :width="isCollapse ? '64px' : '220px'" class="modern-sidebar">
    <div class="sidebar-header">
      <img v-show="!isCollapse" :src="logoImg" alt="logo" class="logo-img" />
      <div v-show="!isCollapse" class="product-name-container">
        <div class="product-name-main">IMLD</div>
        <div class="product-name-sub">智能早筛与</div>
        <div class="product-name-sub">辅助诊断系统</div>
      </div>

      <div
        class="collapse-trigger"
        :title="isCollapse ? '展开菜单' : '折叠菜单'"
        @click="toggleCollapse"
      >
        <el-icon><component :is="isCollapse ? 'Expand' : 'Fold'" /></el-icon>
      </div>
    </div>

    <div v-show="!isCollapse" class="workspace-label">
      <span>{{ BRANDING.workspaceName }}</span>
    </div>

    <el-scrollbar class="sidebar-scrollbar">
      <el-menu
        :default-active="activeMenu"
        class="modern-menu"
        :collapse="isCollapse"
        background-color="transparent"
        text-color="#a6adb4"
        active-text-color="#ffffff"
        unique-opened
        router
        :collapse-transition="false"
      >
        <el-sub-menu
          v-for="group in navigationGroups"
          :key="group.index"
          :index="group.index"
        >
          <template #title>
            <el-icon><component :is="group.icon" /></el-icon>
            <span>{{ group.title }}</span>
          </template>
          <el-menu-item
            v-for="item in group.items"
            :key="item.path"
            :index="item.path"
          >
            <span class="menu-dot"></span>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>

    <div class="sidebar-footer">
      <el-dropdown placement="top" trigger="click" class="user-dropdown">
        <div class="user-profile-card" :class="{ 'is-collapsed': isCollapse }">
          <el-avatar :size="32" :src="doctorAvatar" class="user-avatar" />
          <div v-show="!isCollapse" class="user-info">
            <div class="user-name">{{ userDisplayName }}</div>
            <div class="user-role">{{ userRoleLabel }}</div>
          </div>
          <el-icon v-show="!isCollapse" class="more-icon"><MoreFilled /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="modern-dropdown-menu">
            <el-dropdown-item @click="handleAccountSettings">
              <el-icon><User /></el-icon> 账户设置
            </el-dropdown-item>
            <el-dropdown-item divided class="danger-item" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-aside>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, provide, ref, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import logoImg from '@/assets/logo.svg'
import defaultDoctorAvatar from '@/assets/default-doctor.svg'
import accountApi from '@/api/account'
import { navigationGroups } from '@/app/router/routeCatalog'
import { BRANDING } from '@/constants/branding'
import type { AccountProfileResponse } from '@/types/account'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)
provide('isCollapse', isCollapse)

const activeMenu = ref('/center/patient-list')
const userDisplayName = ref('医生用户')
const userRoleLabel = ref('医生')

const doctorAvatar = computed(() => {
  const storedAvatar = localStorage.getItem('userAvatar')
  if (typeof storedAvatar === 'string' && storedAvatar.trim()) {
    return storedAvatar.trim()
  }
  return defaultDoctorAvatar
})

watchEffect(() => {
  activeMenu.value = route.path
})

const roleLabelMap: Record<string, string> = {
  SYSTEM_ADMIN: '系统管理员',
  COMPLIANCE_AUDITOR: '合规审计员',
  DOCTOR: '医生',
  NURSE: '护士',
  ADMIN: '管理员',
  PATIENT: '患者'
}

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function parseStoredRoles(): string[] {
  try {
    const raw = localStorage.getItem('roleCodes')
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === 'string') : []
  } catch {
    return []
  }
}

function resolveRoleLabel(roleCodes: string[], userType: string | null): string {
  const firstRole = roleCodes.find((role) => roleLabelMap[role])
  if (firstRole) {
    return roleLabelMap[firstRole]
  }

  const normalizedType = typeof userType === 'string' ? userType.trim().toUpperCase() : ''
  return roleLabelMap[normalizedType] || normalizedType || '医生'
}

function persistProfile(profile: AccountProfileResponse): void {
  localStorage.setItem('username', profile.username || '')
  localStorage.setItem('userDisplayName', profile.displayName || profile.username || '')
  localStorage.setItem('userType', profile.userType || '')
  localStorage.setItem('roleCodes', JSON.stringify(profile.roleCodes || []))
  localStorage.setItem('userId', String(profile.userId))
  localStorage.setItem('tenantId', String(profile.tenantId))
}

function refreshUserFromStorage(): void {
  const displayName = localStorage.getItem('userDisplayName') || localStorage.getItem('username') || ''
  const userType = localStorage.getItem('userType')
  const roleCodes = parseStoredRoles()
  userDisplayName.value = displayName.trim() || '医生用户'
  userRoleLabel.value = resolveRoleLabel(roleCodes, userType)
}

async function loadCurrentProfile(): Promise<void> {
  if (!localStorage.getItem('token')) {
    refreshUserFromStorage()
    return
  }

  try {
    const response = await accountApi.getCurrentProfile()
    if (response.data?.code === 200 && response.data.data) {
      persistProfile(response.data.data)
    }
  } catch {
    // The global interceptor handles expired sessions; sidebar identity refresh can fail silently.
  } finally {
    refreshUserFromStorage()
  }
}

function clearSessionStorage(): void {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('username')
  localStorage.removeItem('userDisplayName')
  localStorage.removeItem('userType')
  localStorage.removeItem('roleCodes')
  localStorage.removeItem('userId')
  localStorage.removeItem('tenantId')
  localStorage.removeItem('userAvatar')
}

function handleAccountSettings() {
  router.push('/center/account-settings')
}

function handleLogout() {
  clearSessionStorage()
  refreshUserFromStorage()
  ElMessage({
    message: '您已成功退出！',
    type: 'success',
    duration: 2000
  })
  setTimeout(() => {
    router.push('/')
  }, 1000)
}

onMounted(() => {
  refreshUserFromStorage()
  loadCurrentProfile()
  window.addEventListener('imld:user-profile-updated', refreshUserFromStorage)
})

onBeforeUnmount(() => {
  window.removeEventListener('imld:user-profile-updated', refreshUserFromStorage)
})
</script>

<style scoped>
.modern-sidebar {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--electron-titlebar-safe-top));
  background-color: #001529;
  color: #fff;
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 16px;
  background-color: #001529;
  flex-shrink: 0;
}

.logo-img {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  object-fit: cover;
}

.product-name-container {
  margin-left: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
  white-space: nowrap;
}

.product-name-main {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #ffffff;
  line-height: 1.1;
  margin-bottom: 2px;
}

.product-name-sub {
  font-size: 12px;
  font-weight: 400;
  color: #a6adb4;
  margin-top: 1px;
  line-height: 1.1;
}

.collapse-trigger {
  width: 32px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #8c939d;
  border-radius: 6px;
  transition: all 0.2s;
}

.collapse-trigger:hover {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.1);
}

.workspace-label {
  padding: 16px 20px 8px 20px;
  font-size: 12px;
  color: #6e7a89;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  white-space: nowrap;
}

.sidebar-scrollbar {
  flex: 1;
  overflow-x: hidden;
}

.modern-menu {
  border-right: none;
  padding: 0 8px;
}

:deep(.el-sub-menu__title),
:deep(.el-menu-item) {
  height: 44px !important;
  line-height: 44px !important;
  border-radius: 8px !important;
  margin-bottom: 4px;
  transition: all 0.2s ease;
}

:deep(.el-sub-menu__title:hover),
:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08) !important;
  color: #ffffff !important;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary) !important;
  color: #ffffff !important;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

:deep(.el-menu--inline) {
  background-color: #000c17 !important;
  border-radius: 8px;
  padding: 4px 0;
  margin-bottom: 8px;
}

.menu-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #6e7a89;
  margin-right: 12px;
  margin-left: 2px;
  transition: background-color 0.2s;
}

:deep(.el-menu-item.is-active) .menu-dot {
  background-color: #ffffff;
}

:deep(.el-menu--collapse) {
  padding: 0 4px;
  width: 100%;
}

:deep(.el-menu--collapse) .el-sub-menu__title,
:deep(.el-menu--collapse) .el-menu-item {
  padding: 0 calc(50% - 12px) !important;
}

.sidebar-footer {
  padding: 12px;
  background-color: #001529;
  border-top: 1px solid #112a41;
  flex-shrink: 0;
}

.user-dropdown {
  width: 100%;
}

.user-profile-card {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  width: 100%;
  box-sizing: border-box;
}

.user-profile-card:hover {
  background-color: rgba(255, 255, 255, 0.08);
}

.user-profile-card.is-collapsed {
  justify-content: center;
  padding: 8px 0;
}

.user-avatar {
  flex-shrink: 0;
  border: 1px solid #2a3a4d;
}

.user-info {
  margin-left: 12px;
  flex: 1;
  overflow: hidden;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #8c939d;
  margin-top: 2px;
}

.more-icon {
  color: #8c939d;
  font-size: 16px;
}

.modern-dropdown-menu {
  width: 180px;
  border-radius: 8px;
}

.modern-dropdown-menu .danger-item {
  color: #f56c6c;
}

.modern-dropdown-menu .danger-item:hover {
  color: #f56c6c;
  background-color: #fef0f0;
}
</style>

