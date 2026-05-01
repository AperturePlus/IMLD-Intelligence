<template>
  <view class="page">
    <view class="status-tabs bg-white">
      <view class="tab" :class="status === 'PUBLISHED' ? 'active' : ''" @tap="switchStatus('PUBLISHED')">已发布</view>
      <view class="tab" :class="status === 'PENDING' ? 'active' : ''" @tap="switchStatus('PENDING')">审核中</view>
      <view class="tab" :class="status === 'REJECTED' ? 'active' : ''" @tap="switchStatus('REJECTED')">已驳回</view>
    </view>

    <view v-if="posts.length === 0" class="empty">
      <text v-if="loading">加载中...</text>
      <text v-else>暂无数据</text>
    </view>

    <view
      class="cu-card article no-card"
      v-for="post in posts"
      :key="post.id"
      @tap="goToPostDetail(post.id)"
    >
      <view class="cu-item shadow">
        <view class="title">
          <view class="text-cut">{{ post.title }}</view>
        </view>
        <view class="content" style="margin-top: 6px;">
          <view class="desc">
            <view class="text-content">{{ post.contentExcerpt || '' }}</view>
          </view>
        </view>
        <view class="meta flex justify-between">
          <text>{{ formatTime(post.createdAt) }}</text>
          <text>{{ post.status }}</text>
        </view>
      </view>
    </view>

    <view class="load-more text-center">
      <text v-if="loading">加载中...</text>
      <text v-else-if="finished">没有更多了</text>
    </view>
  </view>
</template>

<script>
import { listPosts } from '@/api/community'
import { getTocUserId } from '@/utils/auth'

export default {
  data() {
    return {
      status: 'PUBLISHED',
      posts: [],
      page: 0,
      size: 20,
      total: 0,
      loading: false,
      finished: false
    }
  },
  onShow() {
    this.resetAndLoad()
  },
  onReachBottom() {
    this.loadMore()
  },
  methods: {
    formatTime(value) {
      if (!value) {
        return ''
      }
      const text = String(value)
      return text.replace('T', ' ').substring(0, 16)
    },
    switchStatus(nextStatus) {
      if (this.status === nextStatus) {
        return
      }
      this.status = nextStatus
      this.resetAndLoad()
    },
    resetAndLoad() {
      this.page = 0
      this.total = 0
      this.posts = []
      this.finished = false
      this.loading = false
      this.loadMore()
    },
    loadMore() {
      if (this.loading || this.finished) {
        return
      }
      const tocUserId = Number(getTocUserId())
      if (!Number.isFinite(tocUserId) || tocUserId <= 0) {
        this.$modal.msgError('登录信息异常，请重新登录')
        uni.reLaunch({ url: '/pages/login' })
        return
      }
      this.loading = true
      listPosts({
        authorTocUserId: tocUserId,
        status: this.status,
        page: this.page,
        size: this.size
      })
        .then((res) => {
          const data = (res && res.data) || {}
          const items = data.items || []
          const total = data.total || 0
          this.total = total
          this.posts = this.page === 0 ? items : this.posts.concat(items)
          this.page += 1
          this.finished = this.posts.length >= this.total
        })
        .catch(() => {
          this.$modal.msgError('加载失败')
        })
        .finally(() => {
          this.loading = false
        })
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
  padding-bottom: 40rpx;
}

.status-tabs {
  display: flex;
  padding: 10rpx 16rpx;
  gap: 10rpx;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  border-radius: 999px;
  background: #f3f4f6;
  color: #333;
  font-size: 26rpx;
}

.tab.active {
  background: rgba(43, 133, 228, 0.12);
  color: #2b85e4;
  font-weight: bold;
}

.empty {
  padding: 40rpx 20rpx;
  text-align: center;
  color: #999;
  font-size: 24rpx;
}

.meta {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #999;
}
</style>

