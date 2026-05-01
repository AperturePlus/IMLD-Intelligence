<template>
  <view class="container">
    <view class="header">
      <view class="user-box">
        <view class="avatar-wrap">
          <image class="avatar" src="/static/images/default-avatar.png" mode="aspectFill"></image>
        </view>
        <view class="info">
          <text class="name">{{ displayName }}</text>
          <text class="sub">ToC 用户 · ID {{ tocUserId || '-' }}</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="menu-item" @tap="goMyPosts">
        <text class="menu-text">我的帖子</text>
        <text class="arrow"></text>
      </view>
      <view class="menu-item" @tap="openPrivacy">
        <text class="menu-text">隐私政策</text>
        <text class="arrow"></text>
      </view>
      <view class="menu-item" @tap="openAgreement">
        <text class="menu-text">用户协议</text>
        <text class="arrow"></text>
      </view>
      <view class="menu-item" @tap="openAbout">
        <text class="menu-text">关于</text>
        <text class="arrow"></text>
      </view>
    </view>

    <view class="logout-wrap">
      <button class="logout-btn" @tap="handleLogout">退出登录</button>
    </view>

    <view class="footer">
      <text class="copyright">© 2026 四川大学华西临床医学院 IMLD 课题组</text>
    </view>
  </view>
</template>

<script>
import { logoutTocSession } from '@/api/tocAuth'
import { clearTocSession, getRefreshToken, getTocNickname, getTocUserId } from '@/utils/auth'

export default {
  data() {
    return {
      tocUserId: '',
      nickname: '',
      globalConfig: getApp().globalData ? getApp().globalData.config : {}
    }
  },
  computed: {
    displayName() {
      if (this.nickname) {
        return this.nickname
      }
      if (this.tocUserId) {
        return `用户${this.tocUserId}`
      }
      return '用户'
    }
  },
  onShow() {
    this.tocUserId = getTocUserId()
    this.nickname = getTocNickname()
  },
  methods: {
    goMyPosts() {
      uni.navigateTo({
        url: '/pages/community/my-posts'
      })
    },
    openPrivacy() {
      const site = this.globalConfig && this.globalConfig.appInfo ? this.globalConfig.appInfo.agreements[0] : null
      if (!site) {
        return
      }
      uni.navigateTo({
        url: `/pages/common/webview/index?title=${site.title}&url=${site.url}`
      })
    },
    openAgreement() {
      const site = this.globalConfig && this.globalConfig.appInfo ? this.globalConfig.appInfo.agreements[1] : null
      if (!site) {
        return
      }
      uni.navigateTo({
        url: `/pages/common/webview/index?title=${site.title}&url=${site.url}`
      })
    },
    openAbout() {
      uni.showModal({
        title: '关于',
        content: 'IMLD 患者社群（MVP）',
        showCancel: false
      })
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        confirmColor: '#fa3534',
        success: (res) => {
          if (!res.confirm) {
            return
          }
          const refreshToken = getRefreshToken()
          if (refreshToken) {
            logoutTocSession(refreshToken).finally(() => {
              clearTocSession()
              uni.reLaunch({ url: '/pages/login' })
            })
            return
          }
          clearTocSession()
          uni.reLaunch({ url: '/pages/login' })
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.container {
  min-height: 100vh;
  background-color: #f4f6f9;
}

.header {
  background: linear-gradient(135deg, #2b85e4 0%, #005eaa 100%);
  padding: 90rpx 40rpx 110rpx;
  border-bottom-left-radius: 40rpx;
  border-bottom-right-radius: 40rpx;
}

.user-box {
  display: flex;
  align-items: center;
}

.avatar-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.4);
  overflow: hidden;
  margin-right: 30rpx;
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  background-color: #e1f0ff;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.name {
  font-size: 40rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 12rpx;
}

.sub {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
}

.card {
  padding: 0 30rpx;
  margin-top: -60rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  margin-left: 30rpx;
  margin-right: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 34rpx 0;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-text {
  font-size: 30rpx;
  color: #333333;
}

.arrow {
  width: 14rpx;
  height: 14rpx;
  border-top: 2px solid #cccccc;
  border-right: 2px solid #cccccc;
  transform: rotate(45deg);
}

.logout-wrap {
  padding: 30rpx;
  margin-top: 16rpx;
}

.logout-btn {
  background-color: #ffffff;
  color: #fa3534;
  font-size: 32rpx;
  font-weight: bold;
  height: 90rpx;
  line-height: 90rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
  border: none;
}

.logout-btn::after {
  border: none;
}

.footer {
  text-align: center;
  padding: 20rpx 0 40rpx;
}

.copyright {
  font-size: 22rpx;
  color: #bbbbbb;
}
</style>

