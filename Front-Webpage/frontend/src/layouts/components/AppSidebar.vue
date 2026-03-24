<template>
  <el-aside :width="isCollapse ? '64px' : '220px'" class="modern-sidebar">
    
    <div class="sidebar-header">
      <img v-show="!isCollapse" :src="avatarImg" alt="logo" class="logo-img" />
      <span v-show="!isCollapse" class="product-name">数智肝循</span>
      
      <div class="collapse-trigger" @click="toggleCollapse" :title="isCollapse ? '展开菜单' : '折叠菜单'">
        <el-icon><component :is="isCollapse ? 'Expand' : 'Fold'" /></el-icon>
      </div>
    </div>

    <div class="workspace-label" v-show="!isCollapse">
      <span>智能医生工作站</span>
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
        <el-sub-menu index="2">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>患者档案</span>
          </template>
          <el-menu-item index="/center/patient-list">
            <span class="menu-dot"></span>
            <span>患者列表</span>
          </el-menu-item>
          <el-menu-item index="/center/patient-record">
            <span class="menu-dot"></span>
            <span>病历录入</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="3">
          <template #title>
            <el-icon><Cpu /></el-icon>
            <span>辅助诊断</span>
          </template>
          <el-menu-item index="/center/ai-diagnosis">
            <span class="menu-dot"></span>
            <span>智能诊断</span>
          </el-menu-item>
          <el-menu-item index="/center/expert-diagnosis">
            <span class="menu-dot"></span>
            <span>专家报告</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="4">
          <template #title>
            <el-icon><FirstAidKit /></el-icon>
            <span>慢病管理</span>
          </template>
          <el-menu-item index="/center/diet">
            <span class="menu-dot"></span>
            <span>膳食干预</span>
          </el-menu-item>
          <el-menu-item index="/center/data-screening">
            <span class="menu-dot"></span>
            <span>筛查数据</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="5">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/center/account-manage">
            <span class="menu-dot"></span>
            <span>账号权限</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>

    <div class="sidebar-footer">
      <el-dropdown placement="top" trigger="click" class="user-dropdown">
        <div class="user-profile-card" :class="{ 'is-collapsed': isCollapse }">
          <el-avatar :size="32" :src="avatarImg" class="user-avatar" />
          <div v-show="!isCollapse" class="user-info">
            <div class="user-name">李医生</div>
            <div class="user-role">主任医师</div>
          </div>
          <el-icon v-show="!isCollapse" class="more-icon"><MoreFilled /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="modern-dropdown-menu">
            <el-dropdown-item>
              <el-icon><User /></el-icon> 账户设置
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout" class="danger-item">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-aside>
</template>

<script setup lang="ts">
import { ref, provide, watchEffect } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import avatarImg from '@/assets/login.webp'

// 这里已经完全删除了 import { UserFilled, Cpu ... } 的代码！

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)
provide('isCollapse', isCollapse)

const activeMenu = ref('/center/patient-list') 

watchEffect(() => {
  activeMenu.value = route.path
})

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleLogout() {
  localStorage.removeItem('token')
  ElMessage({
    message: '您已成功登出！',
    type: 'success',
    duration: 2000
  })
  setTimeout(() => {
    router.push('/')
  }, 1000)
}
</script>

<style scoped>
/* 1. 侧边栏基础布局 (经典的极客深蓝) */
.modern-sidebar {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #001529; 
  color: #fff;
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15); 
  z-index: 100;
  overflow: hidden;
}

/* 2. 顶部 Logo 区 */
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

.product-name {
  margin-left: 12px;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
  white-space: nowrap;
  color: #ffffff;
  flex: 1;
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

/* 3. 工作区小标题 */
.workspace-label {
  padding: 16px 20px 8px 20px;
  font-size: 12px;
  color: #6e7a89; 
  font-weight: 500;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  white-space: nowrap;
}

/* 4. 菜单深度定制 (胶囊样式) */
.sidebar-scrollbar {
  flex: 1;
  overflow-x: hidden;
}

.modern-menu {
  border-right: none;
  padding: 0 8px; 
}

:deep(.el-sub-menu__title), :deep(.el-menu-item) {
  height: 44px !important;
  line-height: 44px !important;
  border-radius: 8px !important; 
  margin-bottom: 4px;
  transition: all 0.2s ease;
}

:deep(.el-sub-menu__title:hover), :deep(.el-menu-item:hover) {
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

/* 5. 底部用户卡片 */
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

/* 6. 修复 Dropdown 下拉菜单的样式 */
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
