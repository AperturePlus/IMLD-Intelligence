<template>
  <view class="admin-profile">
    <!-- 个人信息 -->
    <view class="profile-header">
      <image class="profile-avatar" :src="userInfo.avatar" mode="aspectFill" />
      <view class="profile-info">
        <text class="profile-name">{{ userInfo.name }}</text>
        <text class="profile-phone">{{ userInfo.phone }}</text>
      </view>
    </view>
    
    <!-- 功能按钮 -->
    <view class="profile-actions">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script>
import uniPopup from '@/uni_modules/uni-popup/components/uni-popup/uni-popup.vue'
export default {
  data() {
    return {
      userInfo: {
        name: '管理员',
        phone: '138****8888',
        avatar: 'https://via.placeholder.com/150'
      },
      editUserInfo: {
        name: '',
        phone: ''
      },
      showEditPopup: false
    }
  },
  methods: {
    navigateToAuditLog() {
      uni.navigateTo({
        url: '/pages/admin/audit-log'
      })
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showToast({
              title: '已退出登录',
              icon: 'success'
            })
            setTimeout(() => {
              uni.reLaunch({
                url: '/pages/login'
              })
            }, 1000)
          }
        }
      })
    },
    cancelEdit() {
      this.showEditPopup = false
    },
    saveEdit() {
      this.userInfo.name = this.editUserInfo.name
      this.userInfo.phone = this.editUserInfo.phone
    },
	gotoedit() {
	     uni.navigateTo({
	       url: '/pages/admin/profile-edit?name=' + encodeURIComponent(this.userInfo.name) + '&phone=' + encodeURIComponent(this.userInfo.phone)
	     })
	   }
  }
  
  
 
}
</script>

<style scoped>
.edit-box {
  padding: 20rpx;
}
.form-item {
  margin-bottom: 20rpx;
}
.form-actions {
  display: flex;
  justify-content: space-between;
}
</style>
</script>

<style scoped>
.admin-profile {
  padding: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-right: 20px;
}

.profile-info {
  display: flex;
  flex-direction: column;
}

.profile-name {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.profile-phone {
  font-size: 15px;
  color: #666;
}

.profile-actions {
  background-color: #fff;
  border-radius: 12px;
  padding: 0 15px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.action-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
}

.action-item:last-child {
  border-bottom: none;
}

.action-text {
  font-size: 16px;
  color: #333;
}

.action-icon {
  width: 16px;
  height: 16px;
}

.logout-btn {
  margin-top: 30px;
  background-color: #fef0f0;
  color: #f56c6c;
  height: 44px;
  line-height: 44px;
  font-size: 16px;
  border-radius: 8px;
}
</style>