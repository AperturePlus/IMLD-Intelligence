<template>
  <view class="page">
    <view class="header">
      <text class="title">通知中心</text>
      <view class="filter-tabs">
        <text
          class="filter-tab"
          :class="filter === 'all' ? 'active' : ''"
          @click="filter = 'all'"
        >全部</text>
        <text
          class="filter-tab"
          :class="filter === 'unread' ? 'active' : ''"
          @click="filter = 'unread'"
        >未读</text>
      </view>
    </view>

    <view v-if="notifications.length === 0" class="empty-tip">
      <text v-if="loading">加载中...</text>
      <text v-else>暂无通知</text>
    </view>

    <view v-else>
      <view
        class="notification-card"
        v-for="n in notifications"
        :key="n.id"
        :class="n.isRead ? 'read' : 'unread'"
        @tap="handleTap(n)"
      >
        <view class="n-header">
          <text class="n-type">{{ typeLabel(n.type) }}</text>
          <text class="n-time">{{ formatTime(n.createdAt) }}</text>
        </view>
        <view class="n-title">{{ n.title }}</view>
        <view class="n-content">{{ n.content }}</view>
      </view>

      <view class="load-more text-center">
        <text v-if="loading">加载中...</text>
        <text v-else-if="finished">没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script>
import { listMyNotifications, markNotificationRead } from '@/api/community'
import { getTocUserId } from '@/utils/auth'

export default {
  data() {
    return {
      notifications: [],
      filter: 'all',
      page: 0,
      size: 20,
      total: 0,
      loading: false,
      finished: false
    }
  },
  watch: {
    filter() {
      this.page = 0
      this.total = 0
      this.notifications = []
      this.finished = false
      this.loadNotifications()
    }
  },
  onLoad() {
    this.loadNotifications()
  },
  onReachBottom() {
    this.loadMore()
  },
  methods: {
    formatTime(value) {
      if (!value) return ''
      return String(value).replace('T', ' ').substring(0, 16)
    },
    typeLabel(type) {
      const map = {
        COMMENT: '评论',
        LIKE: '点赞',
        BOOKMARK: '收藏',
        SYSTEM: '系统'
      }
      return map[type] || type || '通知'
    },
    loadNotifications() {
      const tocUserId = Number(getTocUserId())
      if (!Number.isFinite(tocUserId) || tocUserId <= 0) {
        this.$modal.msgError('登录信息异常')
        return
      }
      this.loading = true
      const params = {
        tocUserId,
        page: this.page,
        size: this.size
      }
      if (this.filter === 'unread') {
        params.isRead = false
      }
      listMyNotifications(params)
        .then((res) => {
          const data = (res && res.data) || {}
          const items = data.items || []
          const total = data.total || 0
          this.total = total
          this.notifications = this.page === 0 ? items : this.notifications.concat(items)
          this.page += 1
          this.finished = this.notifications.length >= this.total
        })
        .catch(() => {
          this.$modal.msgError('加载通知失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadMore() {
      if (this.loading || this.finished) return
      this.loadNotifications()
    },
    handleTap(notification) {
      if (!notification.isRead) {
        markNotificationRead(notification.id)
          .then(() => {
            notification.isRead = true
          })
          .catch(() => {
            // ignore
          })
      }
      if (notification.relatedPostId) {
        uni.navigateTo({
          url: `/pages/community/post-detail?postId=${notification.relatedPostId}`
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f5f6f7;
  padding: 20rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
}

.title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.filter-tabs {
  display: flex;
  gap: 16rpx;
}

.filter-tab {
  font-size: 26rpx;
  color: #666;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.filter-tab.active {
  color: #2b85e4;
  background: rgba(43, 133, 228, 0.1);
  font-weight: bold;
}

.empty-tip {
  padding: 60rpx 20rpx;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}

.notification-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.notification-card.unread {
  border-left: 6rpx solid #2b85e4;
}

.notification-card.read {
  opacity: 0.85;
}

.n-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.n-type {
  font-size: 24rpx;
  color: #2b85e4;
  background: rgba(43, 133, 228, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.n-time {
  font-size: 22rpx;
  color: #999;
}

.n-title {
  margin-top: 12rpx;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.n-content {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.load-more {
  padding: 30rpx 0;
  text-align: center;
  color: #999;
  font-size: 24rpx;
}
</style>
