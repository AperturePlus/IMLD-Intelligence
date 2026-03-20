<template>
  <view class="admin-container">
    <!-- 顶部标题 -->
    <view class="admin-header">
      <text class="admin-title">管理员控制台</text>
    </view>
    
    <!-- 内容区域 -->
    <view class="admin-content">
      <template v-if="currentTab === 'news'">
        <news-management />
      </template>
      <template v-else-if="currentTab === 'audit'">
        <audit-management />
      </template>
      <template v-else-if="currentTab === 'user'">
        <user-management />
      </template>
      <template v-else-if="currentTab === 'report'">
        <report-management />
      </template>
      <template v-else-if="currentTab === 'profile'">
        <admin-profile />
      </template>
    </view>
    
    <!-- 自定义底部导航栏 -->
    <view class="custom-tabbar">
      <view 
        class="tabbar-item" 
        :class="{ active: currentTab === 'news' }"
        @click="switchTab('news')"
      >
        <image 
          :src="currentTab === 'news' /*? '/static/admin/news-active.png' : '/static/admin/news.png'*/" 
          class="tabbar-icon"
        />
        <text class="tabbar-text">资讯管理</text>
      </view>
      <view 
        class="tabbar-item" 
        :class="{ active: currentTab === 'audit' }"
        @click="switchTab('audit')"
      >
        <image 
          :src="currentTab === 'audit' /*? '/static/admin/audit-active.png' : '/static/admin/audit.png'*/" 
          class="tabbar-icon"
        />
        <text class="tabbar-text">审核管理</text>
      </view>
      <view 
        class="tabbar-item" 
        :class="{ active: currentTab === 'user' }"
        @click="switchTab('user')"
      >
        <image 
          :src="currentTab === 'user' /*? '/static/admin/user-active.png' : '/static/admin/user.png'*/" 
          class="tabbar-icon"
        />
        <text class="tabbar-text">用户管理</text>
      </view>
      <view 
        class="tabbar-item" 
        :class="{ active: currentTab === 'report' }"
        @click="switchTab('report')"
      >
        <image 
          :src="currentTab === 'report'/* ? '/static/admin/report-active.png' : '/static/admin/report.png'*/" 
          class="tabbar-icon"
        />
        <text class="tabbar-text">举报管理</text>
      </view>
      <view 
        class="tabbar-item" 
        :class="{ active: currentTab === 'profile' }"
        @click="switchTab('profile')"
      >
        <image 
          :src="currentTab === 'profile' /*? '/static/admin/profile-active.png' : '/static/admin/profile.png'*/" 
          class="tabbar-icon"
        />
        <text class="tabbar-text">个人中心</text>
      </view>
    </view>
  </view>
</template>

<script>
import NewsManagement from '@/components/admin/NewsManagement.vue'
import AuditManagement from '@/components/admin/AuditManagement.vue'
import UserManagement from '@/components/admin/UserManagement.vue'
import ReportManagement from '@/components/admin/ReportManagement.vue'
import AdminProfile from '@/components/admin/AdminProfile.vue'

export default {
  components: {
    NewsManagement,
    AuditManagement,
    UserManagement,
    ReportManagement,
    AdminProfile
  },
  data() {
    return {
      currentTab: 'news' // 默认显示资讯管理
    }
  },
  methods: {
    switchTab(tabName) {
      this.currentTab = tabName
    }
  }
}
</script>

<style>
/* 管理员页面整体样式 */
.admin-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f7;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

/* 头部样式 */
.admin-header {
  padding: 15px;
  background-color: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.admin-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

/* 内容区域样式 */
.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

/* 自定义底部导航栏样式 */
.custom-tabbar {
  display: flex;
  height: 60px;
  background-color: #ffffff;
  border-top: 1px solid #f0f0f0;
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 5px 0;
}

.tabbar-item.active {
  color: #409EFF;
}

.tabbar-icon {
  width: 24px;
  height: 24px;
  margin-bottom: 3px;
}

.tabbar-text {
  font-size: 12px;
  color: inherit;
}
</style>