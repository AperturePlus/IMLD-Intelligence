<template>
  <view class="page">
    <view class="header">
      <text class="title">我的收藏</text>
    </view>

    <view v-if="bookmarks.length === 0" class="empty-tip">
      <text v-if="loading">加载中...</text>
      <text v-else>暂无收藏</text>
    </view>

    <view v-else>
      <view class="post-card" v-for="post in bookmarks" :key="post.id" @tap="goToPostDetail(post.id)">
        <view class="post-title">{{ post.title }}</view>
        <view class="post-excerpt">{{ post.contentExcerpt || '' }}</view>
        <view class="post-meta">
          <text>{{ post.authorDisplayName || '用户' }} · {{ formatTime(post.createdAt) }}</text>
        </view>
      </view>

      <view class="load-more text-center">
        <text v-if="loading">加载中...</text>
        <text v-else-if="finished">没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script>
import { listMyBookmarks } from '@/api/community'
import { getTocUserId } from '@/utils/auth'

export default {
  data() {
    return {
      bookmarks: [],
      page: 0,
      size: 20,
      total: 0,
      loading: false,
      finished: false
    }
  },
  onLoad() {
    this.loadBookmarks()
  },
  onReachBottom() {
    this.loadMore()
  },
  methods: {
    formatTime(value) {
      if (!value) return ''
      return String(value).replace('T', ' ').substring(0, 16)
    },
    loadBookmarks() {
      const tocUserId = Number(getTocUserId())
      if (!Number.isFinite(tocUserId) || tocUserId <= 0) {
        this.$modal.msgError('登录信息异常')
        return
      }
      this.loading = true
      listMyBookmarks({ tocUserId, page: this.page, size: this.size })
        .then((res) => {
          const data = (res && res.data) || {}
          const items = data.items || []
          const total = data.total || 0
          this.total = total
          this.bookmarks = this.page === 0 ? items : this.bookmarks.concat(items)
          this.page += 1
          this.finished = this.bookmarks.length >= this.total
        })
        .catch(() => {
          this.$modal.msgError('加载收藏失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    loadMore() {
      if (this.loading || this.finished) return
      this.loadBookmarks()
    },
    goToPostDetail(postId) {
      uni.navigateTo({
        url: `/pages/community/post-detail?postId=${postId}`
      })
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
  padding: 20rpx 0;
}

.title {
  font-size: 34rpx;
  font-weight: bold;
  color: #333;
}

.empty-tip {
  padding: 60rpx 20rpx;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}

.post-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.post-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.post-excerpt {
  margin-top: 10rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #999;
}

.load-more {
  padding: 30rpx 0;
  text-align: center;
  color: #999;
  font-size: 24rpx;
}
</style>
