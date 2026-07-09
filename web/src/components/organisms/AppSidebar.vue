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
        <BaseIcon :name="isCollapse ? 'expand' : 'fold'" :size="16" />
      </div>
    </div>

    <div v-show="!isCollapse" class="workspace-label">
      <span>{{ BRANDING.workspaceName }}</span>
    </div>

    <div v-if="showBrowserHomeShortcut" class="home-shortcut-wrapper">
      <button
        type="button"
        class="home-shortcut"
        :class="{ 'is-active': isWelcomeActive, 'is-collapsed': isCollapse }"
        :title="isCollapse ? '返回首页' : undefined"
        :disabled="isWelcomeActive"
        @click="goWelcome"
      >
        <BaseIcon name="house" :size="16" class="home-shortcut-icon" />
        <span v-show="!isCollapse">首页</span>
      </button>
    </div>

    <el-scrollbar class="sidebar-scrollbar">
      <el-menu
        :default-active="activeMenu"
        class="modern-menu"
        :collapse="isCollapse"
        background-color="transparent"
        text-color="var(--imld-on-dark-secondary)"
        active-text-color="var(--imld-white)"
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
          <BaseAvatar :size="32" :src="doctorAvatar" class="user-avatar" />
          <div v-show="!isCollapse" class="user-info">
            <div class="user-name">{{ userDisplayName }}</div>
            <div class="user-role">{{ userRoleLabel }}</div>
          </div>
          <el-icon v-show="!isCollapse" class="more-icon"
            ><MoreFilled
          /></el-icon>
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
import {
  computed,
  onBeforeUnmount,
  onMounted,
  provide,
  ref,
  watchEffect,
} from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import logoImg from "@/assets/logo.svg";
import defaultDoctorAvatar from "@/assets/default-doctor.svg";
import accountApi from "@/api/account";
import {
  centerRouteDefinitions,
  navigationGroups,
} from "@/app/router/routeCatalog";
import { BRANDING } from "@/constants/branding";
import BaseAvatar from "@/components/atoms/BaseAvatar.vue";
import BaseIcon from "@/components/atoms/BaseIcon.vue";
import type { AccountProfileResponse } from "@/types/account";

const router = useRouter();
const route = useRoute();
const isCollapse = ref(false);
provide("isCollapse", isCollapse);

const isElectronShell = computed(
  () => typeof window !== "undefined" && Boolean(window.electron?.shell)
);
const showBrowserHomeShortcut = computed(() => !isElectronShell.value);
const welcomePath = centerRouteDefinitions.welcome.fullPath;
const isWelcomeActive = computed(() => route.path === welcomePath);

const activeMenu = ref("/center/patient-list");
const userDisplayName = ref("医生用户");
const userRoleLabel = ref("医生");

const doctorAvatar = computed(() => {
  const storedAvatar = localStorage.getItem("userAvatar");
  if (typeof storedAvatar === "string" && storedAvatar.trim()) {
    return storedAvatar.trim();
  }
  return defaultDoctorAvatar;
});

watchEffect(() => {
  activeMenu.value = route.path;
});

const roleLabelMap: Record<string, string> = {
  SYSTEM_ADMIN: "系统管理员",
  COMPLIANCE_AUDITOR: "合规审计员",
  DOCTOR: "医生",
  NURSE: "护士",
  ADMIN: "管理员",
  PATIENT: "患者",
};

function toggleCollapse() {
  isCollapse.value = !isCollapse.value;
}

function goWelcome() {
  if (isWelcomeActive.value) {
    return;
  }
  router.push(welcomePath);
}

function parseStoredRoles(): string[] {
  try {
    const raw = localStorage.getItem("roleCodes");
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed)
      ? parsed.filter((item): item is string => typeof item === "string")
      : [];
  } catch {
    return [];
  }
}

function resolveRoleLabel(
  roleCodes: string[],
  userType: string | null
): string {
  const firstRole = roleCodes.find((role) => roleLabelMap[role]);
  if (firstRole) {
    return roleLabelMap[firstRole];
  }

  const normalizedType =
    typeof userType === "string" ? userType.trim().toUpperCase() : "";
  return roleLabelMap[normalizedType] || normalizedType || "医生";
}

function persistProfile(profile: AccountProfileResponse): void {
  localStorage.setItem("username", profile.username || "");
  localStorage.setItem(
    "userDisplayName",
    profile.displayName || profile.username || ""
  );
  localStorage.setItem("userType", profile.userType || "");
  localStorage.setItem("roleCodes", JSON.stringify(profile.roleCodes || []));
  localStorage.setItem("userId", String(profile.userId));
  localStorage.setItem("tenantId", String(profile.tenantId));
}

function refreshUserFromStorage(): void {
  const displayName =
    localStorage.getItem("userDisplayName") ||
    localStorage.getItem("username") ||
    "";
  const userType = localStorage.getItem("userType");
  const roleCodes = parseStoredRoles();
  userDisplayName.value = displayName.trim() || "医生用户";
  userRoleLabel.value = resolveRoleLabel(roleCodes, userType);
}

async function loadCurrentProfile(): Promise<void> {
  if (!localStorage.getItem("token")) {
    refreshUserFromStorage();
    return;
  }

  try {
    const response = await accountApi.getCurrentProfile();
    if (response.data?.code === 200 && response.data.data) {
      persistProfile(response.data.data);
    }
  } catch {
    // The global interceptor handles expired sessions; sidebar identity refresh can fail silently.
  } finally {
    refreshUserFromStorage();
  }
}

function clearSessionStorage(): void {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("username");
  localStorage.removeItem("userDisplayName");
  localStorage.removeItem("userType");
  localStorage.removeItem("roleCodes");
  localStorage.removeItem("userId");
  localStorage.removeItem("tenantId");
  localStorage.removeItem("userAvatar");
}

function handleAccountSettings() {
  router.push("/center/account-settings");
}

function handleLogout() {
  clearSessionStorage();
  refreshUserFromStorage();
  ElMessage({
    message: "您已成功退出！",
    type: "success",
    duration: 2000,
  });
  setTimeout(() => {
    router.push("/");
  }, 1000);
}

onMounted(() => {
  refreshUserFromStorage();
  loadCurrentProfile();
  window.addEventListener("imld:user-profile-updated", refreshUserFromStorage);
});

onBeforeUnmount(() => {
  window.removeEventListener(
    "imld:user-profile-updated",
    refreshUserFromStorage
  );
});
</script>

<style scoped>
.modern-sidebar {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--electron-titlebar-safe-top));
  background-color: var(--imld-navy);
  color: var(--imld-white);
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
  background-color: var(--imld-navy);
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
  color: var(--imld-white);
  line-height: 1.1;
  margin-bottom: 2px;
}

.product-name-sub {
  font-size: 12px;
  font-weight: 400;
  color: var(--imld-on-dark-secondary);
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
  color: var(--imld-on-dark-tertiary);
  border-radius: 6px;
  transition: all 0.2s;
}
.collapse-trigger:hover {
  color: var(--imld-white);
  background-color: rgba(var(--imld-white-rgb), 0.1);
}

.workspace-label {
  padding: 16px 20px 8px 20px;
  font-size: 12px;
  color: var(--imld-on-dark-quaternary);
  font-weight: 500;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  white-space: nowrap;
}

.home-shortcut-wrapper {
  padding: 0 12px 10px;
  flex-shrink: 0;
}

.home-shortcut {
  width: 100%;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  padding: 0 12px;
  border: 1px solid var(--imld-dark-border);
  border-radius: 8px;
  background: rgba(var(--imld-white-rgb), 0.03);
  color: var(--imld-on-dark-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.home-shortcut:hover:not(:disabled) {
  color: var(--imld-white);
  border-color: rgba(64, 158, 255, 0.5);
  background: rgba(64, 158, 255, 0.2);
}

.home-shortcut:disabled {
  cursor: default;
}

.home-shortcut.is-active {
  color: var(--imld-white);
  border-color: var(--el-color-primary);
  background: rgba(64, 158, 255, 0.26);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.24);
}

.home-shortcut.is-collapsed {
  justify-content: center;
  padding: 0;
}

.home-shortcut-icon {
  font-size: 16px;
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
  background-color: rgba(var(--imld-white-rgb), 0.08) !important;
  color: var(--imld-white) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary) !important;
  color: var(--imld-white) !important;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

:deep(.el-menu--inline) {
  background-color: var(--imld-navy-deep) !important;
  border-radius: 8px;
  padding: 4px 0;
  margin-bottom: 8px;
}

.menu-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--imld-on-dark-quaternary);
  margin-right: 12px;
  margin-left: 2px;
  transition: background-color 0.2s;
}
:deep(.el-menu-item.is-active) .menu-dot {
  background-color: var(--imld-white);
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
  background-color: var(--imld-navy);
  border-top: 1px solid var(--imld-dark-border);
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
  background-color: rgba(var(--imld-white-rgb), 0.08);
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
  color: var(--imld-white);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: var(--imld-on-dark-tertiary);
  margin-top: 2px;
}

.more-icon {
  color: var(--imld-on-dark-tertiary);
  font-size: 16px;
}

.modern-dropdown-menu {
  width: 180px;
  border-radius: 8px;
}

.modern-dropdown-menu .danger-item {
  color: var(--imld-danger);
}

.modern-dropdown-menu .danger-item:hover {
  color: var(--imld-danger);
  background-color: #fef0f0;
}
</style>
